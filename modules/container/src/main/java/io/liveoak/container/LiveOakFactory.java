/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.container;

import io.liveoak.client.DefaultClient;
import io.liveoak.common.codec.ResourceCodec;
import io.liveoak.common.codec.ResourceCodecManager;
import io.liveoak.common.codec.ResourceDecoder;
import io.liveoak.common.codec.StateEncoder;
import io.liveoak.common.codec.html.HTMLEncoder;
import io.liveoak.common.codec.json.JSONDecoder;
import io.liveoak.common.codec.json.JSONEncoder;
import io.liveoak.container.extension.ExtensionInstaller;
import io.liveoak.container.extension.ExtensionLoader;
import io.liveoak.container.interceptor.InterceptorManager;
import io.liveoak.container.interceptor.TimingInterceptor;
import io.liveoak.container.protocols.PipelineConfigurator;
import io.liveoak.container.service.*;
import io.liveoak.container.tenancy.GlobalContext;
import io.liveoak.container.tenancy.service.ApplicationRegistryService;
import io.liveoak.container.tenancy.service.ApplicationsDirectoryService;
import io.liveoak.container.zero.extension.ZeroExtension;
import io.liveoak.container.zero.service.ZeroBootstrapper;
import io.liveoak.spi.LiveOak;
import io.liveoak.spi.MediaType;
import io.liveoak.spi.client.Client;
import io.liveoak.spi.container.SubscriptionManager;
import io.liveoak.spi.container.interceptor.Interceptor;
import org.jboss.logging.Logger;
import org.jboss.msc.service.*;
import org.jboss.msc.value.ImmediateValue;
import org.vertx.java.core.Vertx;
import org.vertx.java.platform.PlatformManager;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.Executor;

import static io.liveoak.spi.LiveOak.*;

/**
 * Bootstrapping <code>main()</code> method.
 *
 * @author Bob McWhirter
 */
public class LiveOakFactory {

    private static final Logger log = Logger.getLogger(LiveOakFactory.class);

    public static LiveOakSystem create() throws Exception {
        return create(null, null, null);
    }

    public static LiveOakSystem create(Vertx vertx) throws Exception {
        return create(null, null, vertx);
    }

    public static LiveOakSystem create(File configDir, File applicationsDir) throws Exception {
        return create(configDir, applicationsDir, null);
    }

    public static LiveOakSystem create(File configDir, File applicationsDir, Vertx vertx) throws Exception {
        return new LiveOakFactory(configDir, applicationsDir, vertx).createInternal();
    }

    // ----------------------------------------------------------------------
    // ----------------------------------------------------------------------

    private LiveOakFactory(File configDir, File applicationsDir, Vertx vertx) {
        this.configDir = configDir;
        this.appsDir = applicationsDir;
        this.vertx = vertx;
        this.serviceContainer = ServiceContainer.Factory.create();
        serviceContainer.addListener(new AbstractServiceListener<Object>() {
            @Override
            public void transition(ServiceController<?> controller, ServiceController.Transition transition) {
                if (transition.getAfter().equals(ServiceController.Substate.START_FAILED)) {
                    log.errorf(controller.getStartException(), "Unable to start service: %s", controller.getName());
                    controller.getStartException().printStackTrace();
                }
            }
        });
    }

    public LiveOakSystem createInternal() throws Exception {
        prolog();
        createTenancy();
        createServers();
        createClient();
        createExtensions();
        createVertx();
        installCodecs();
        serviceContainer.awaitStability();
        return (LiveOakSystem) serviceContainer.getService(LIVEOAK).awaitValue();
    }

    protected void prolog() {
        LiveOakSystem system = new LiveOakSystem(serviceContainer);
        serviceContainer.addService(LIVEOAK, new ValueService<LiveOakSystem>(new ImmediateValue<>(system)))
                .install();

        serviceContainer.addService(SERVICE_REGISTRY,
                new ValueService<ServiceRegistry>(
                        new ImmediateValue<>(serviceContainer)
                ))
                .install();

        serviceContainer.addService(SERVICE_CONTAINER,
                new ValueService<ServiceRegistry>(
                        new ImmediateValue<>(serviceContainer)
                ))
                .install();

    }

    protected void createTenancy() {
        serviceContainer.addService( APPLICATIONS_DIR, new ApplicationsDirectoryService( this.appsDir ) )
                .install();

        serviceContainer.addService(APPLICATION_REGISTRY, new ApplicationRegistryService())
                .install();

        Service<GlobalContext> globalContext = new ValueService<GlobalContext>(new ImmediateValue<>(new GlobalContext()));
        serviceContainer.addService(GLOBAL_CONTEXT, globalContext)
                .install();
    }

    protected void createExtensions() {
        ExtensionInstaller installer = new ExtensionInstaller(serviceContainer, LiveOak.resource(ZeroExtension.APPLICATION_ID, "system"));
        serviceContainer.addService(EXTENSION_INSTALLER,
                new ValueService<ExtensionInstaller>(
                        new ImmediateValue<>(installer)
                ))
                .install();

        ExtensionLoader extensionLoader = new ExtensionLoader(new File( configDir, "extensions" ).getAbsoluteFile() );

        serviceContainer.addService(EXTENSION_LOADER, extensionLoader)
                .addDependency(EXTENSION_INSTALLER, ExtensionInstaller.class, extensionLoader.extensionInstallerInjector())
                .install();

        ZeroBootstrapper zero = new ZeroBootstrapper();

        serviceContainer.addService( LiveOak.LIVEOAK.append( "zero", "bootstrapper" ), zero )
                .addDependency(EXTENSION_INSTALLER, ExtensionInstaller.class, zero.extensionInstallerInjector() )
                .install();
    }

    protected void createServers() throws UnknownHostException {
        UnsecureServerService unsecureServer = new UnsecureServerService();
        serviceContainer.addService(server("unsecure", true), unsecureServer)
                .addDependency(PIPELINE_CONFIGURATOR, PipelineConfigurator.class, unsecureServer.pipelineConfiguratorInjector())
                .addInjection(unsecureServer.hostInjector(), InetAddress.getByName("localhost"))
                .addInjection(unsecureServer.portInjector(), 8080)
                .install();

        LocalServerService localServer = new LocalServerService();
        serviceContainer.addService(server("local", false), localServer)
                .addDependency(PIPELINE_CONFIGURATOR, PipelineConfigurator.class, localServer.pipelineConfiguratorInjector())
                .install();


        SubscriptionManagerService subscriptionManager = new SubscriptionManagerService();

        serviceContainer.addService(SUBSCRIPTION_MANAGER, subscriptionManager)
                .install();

        ValueService<InterceptorManager> interceptorManager = new ValueService<>(new ImmediateValue<InterceptorManager>(new InterceptorManager()));
        serviceContainer.addService(INTERCEPTOR_MANAGER, interceptorManager)
                .install();

        CodecManagerService codecManager = new CodecManagerService();
        serviceContainer.addService(CODEC_MANAGER, codecManager)
                .install();

        WorkerPoolService workerPool = new WorkerPoolService();
        serviceContainer.addService(WORKER_POOL, workerPool)
                .install();

        PipelineConfiguratorService pipelineConfigurator = new PipelineConfiguratorService();
        ServiceBuilder<PipelineConfigurator> pipelineBuilder = serviceContainer.addService(PIPELINE_CONFIGURATOR, pipelineConfigurator)
                .addDependency(SUBSCRIPTION_MANAGER, SubscriptionManager.class, pipelineConfigurator.subscriptionManagerInjector())
                .addDependency(INTERCEPTOR_MANAGER, InterceptorManager.class, pipelineConfigurator.interceptorManagerInjector())
                .addDependency(CODEC_MANAGER, ResourceCodecManager.class, pipelineConfigurator.codecManagerInjector())
                .addDependency(CLIENT, Client.class, pipelineConfigurator.clientInjector())
                .addDependency(GLOBAL_CONTEXT, GlobalContext.class, pipelineConfigurator.globalContextInjector())
                .addDependency(WORKER_POOL, Executor.class, pipelineConfigurator.workerPoolInjector());

        pipelineBuilder.install();


        NotifierService notifier = new NotifierService();
        serviceContainer.addService(NOTIFIER, notifier)
                .addDependency(SUBSCRIPTION_MANAGER, SubscriptionManager.class, notifier.subscriptionManagerInjector())
                .install();

    }

    protected void createClient() {
        ClientService client = new ClientService();
        serviceContainer.addService(CLIENT, client)
                .install();

        ClientConnectorService clientConnector = new ClientConnectorService();
        serviceContainer.addService(CLIENT.append("connect"), clientConnector)
                .addDependency(CLIENT, DefaultClient.class, clientConnector.clientInjector())
                .addDependency(server("local", false))
                .install();
    }

    protected void createVertx() {
        if (vertx == null) {
            VertxService vertxSvc = new VertxService();
            serviceContainer.addService(VERTX, vertxSvc)
                    .addDependency(VERTX_PLATFORM_MANAGER, PlatformManager.class, vertxSvc.platformManagerInjector())
                    .install();

            serviceContainer.addService(VERTX_PLATFORM_MANAGER, new PlatformManagerService())
                    .install();
        } else {
            serviceContainer.addService(VERTX, new ValueService<Vertx>(new ImmediateValue<>(vertx)))
                    .install();
        }
    }


    protected void installCodecs() {
        installInterceptor(serviceContainer, "timing", new TimingInterceptor());

        installCodec(serviceContainer, MediaType.JSON, JSONEncoder.class, new JSONDecoder());

        installCodec(serviceContainer, MediaType.HTML, HTMLEncoder.class, null);
    }


    private static void installInterceptor(ServiceContainer serviceContainer, String name, Interceptor interceptor) {
        ServiceName serviceName = interceptor(name);

        ServiceController<Interceptor> controller = serviceContainer.addService(serviceName, new ValueService<Interceptor>(new ImmediateValue<Interceptor>(interceptor)))
                .install();

        installInterceptor(serviceContainer, controller);
    }

    private static void installInterceptor(ServiceContainer serviceContainer, ServiceController<Interceptor> interceptor) {
        ServiceName serviceName = interceptor.getName();
        InterceptorRegistrationService registration = new InterceptorRegistrationService();
        serviceContainer.addService(serviceName.append("register"), registration)
                .addDependency(INTERCEPTOR_MANAGER, InterceptorManager.class, registration.interceptorManagerInjector())
                .addDependency(serviceName, Interceptor.class, registration.interceptorInjector())
                .install();
    }


    private static void installCodec(ServiceContainer serviceContainer, MediaType mediaType, Class<? extends StateEncoder> encoderClass, ResourceDecoder decoder) {
        ServiceName name = codec(mediaType.toString());

        CodecService codec = new CodecService(encoderClass, decoder);
        serviceContainer.addService(name, codec)
                .install();

        CodecInstallationService installer = new CodecInstallationService(mediaType);
        serviceContainer.addService(name.append("install"), installer)
                .addDependency(name, ResourceCodec.class, installer.codecInjector())
                .addDependency(CODEC_MANAGER, ResourceCodecManager.class, installer.codecManagerInjector())
                .install();
    }

    private final File configDir;
    private final File appsDir;
    private final Vertx vertx;
    private final ServiceContainer serviceContainer;

}
