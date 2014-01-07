/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.container;

import java.io.File;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

import io.liveoak.client.DefaultClient;
import io.liveoak.common.codec.Encoder;
import io.liveoak.common.codec.ResourceCodec;
import io.liveoak.common.codec.ResourceCodecManager;
import io.liveoak.common.codec.ResourceDecoder;
import io.liveoak.common.codec.html.HTMLEncoder;
import io.liveoak.common.codec.json.JSONDecoder;
import io.liveoak.common.codec.json.JSONEncoder;
import io.liveoak.container.deploy.DefaultDeployer;
import io.liveoak.container.deploy.DirectDeployer;
import io.liveoak.container.deploy.DirectoryDeploymentManager;
import io.liveoak.container.deploy.factory.ClasspathBasedResourceFactory;
import io.liveoak.container.deploy.factory.ModuleBasedResourceFactory;
import io.liveoak.container.interceptor.InterceptorManager;
import io.liveoak.container.interceptor.TimingInterceptor;
import io.liveoak.container.protocols.PipelineConfigurator;
import io.liveoak.container.server.LocalServer;
import io.liveoak.container.service.ClientConnectorService;
import io.liveoak.container.service.ClientService;
import io.liveoak.container.service.CodecInstallationService;
import io.liveoak.container.service.CodecManagerService;
import io.liveoak.container.service.CodecService;
import io.liveoak.container.service.ContainerService;
import io.liveoak.container.service.DeployerService;
import io.liveoak.container.service.DeploymentManagerService;
import io.liveoak.container.service.DirectDeployerService;
import io.liveoak.container.service.InterceptorRegistrationService;
import io.liveoak.container.service.InternalResourceDeploymentService;
import io.liveoak.container.service.LocalServerService;
import io.liveoak.container.service.NotifierService;
import io.liveoak.container.service.PipelineConfiguratorService;
import io.liveoak.container.service.PlatformManagerService;
import io.liveoak.container.service.RootResourceFactoryRegistrationService;
import io.liveoak.container.service.SubscriptionManagerService;
import io.liveoak.container.service.UnsecureServerService;
import io.liveoak.container.service.VertxService;
import io.liveoak.container.service.WorkerPoolService;
import io.liveoak.spi.Container;
import io.liveoak.spi.MediaType;
import io.liveoak.spi.client.Client;
import io.liveoak.spi.container.Deployer;
import io.liveoak.spi.container.RootResourceFactory;
import io.liveoak.spi.container.SubscriptionManager;
import io.liveoak.spi.container.interceptor.Interceptor;
import io.liveoak.spi.resource.RootResource;
import org.jboss.logging.Logger;
import org.jboss.msc.service.AbstractServiceListener;
import org.jboss.msc.service.ServiceBuilder;
import org.jboss.msc.service.ServiceContainer;
import org.jboss.msc.service.ServiceController;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.ValueService;
import org.jboss.msc.value.ImmediateValue;
import org.vertx.java.core.Vertx;
import org.vertx.java.platform.PlatformManager;
import sun.misc.Service;

import static io.liveoak.container.LiveOak.*;

/**
 * Bootstrapping <code>main()</code> method.
 *
 * @author Bob McWhirter
 */
public class LiveOakFactory {

    private static final Logger log = Logger.getLogger(LiveOakFactory.class);

    public static LiveOakSystem create() throws Exception {
        return create(null, null);
    }

    public static LiveOakSystem create(Vertx vertx) throws Exception {
        return create(null, vertx);
    }

    public static LiveOakSystem create(File configDir) throws Exception {
        return create(configDir, null);
    }

    public static LiveOakSystem create(File configDir, Vertx vertx) throws Exception {
        ServiceContainer serviceContainer = ServiceContainer.Factory.create();
        LiveOakSystem system = new LiveOakSystem(serviceContainer);

        serviceContainer.addListener(new AbstractServiceListener<Object>() {
            @Override
            public void transition(ServiceController<?> controller, ServiceController.Transition transition) {
                if (transition.getAfter().equals(ServiceController.Substate.START_FAILED)) {
                    log.errorf(controller.getStartException(), "Unable to start service: %s", controller.getName());
                }
            }
        });

        serviceContainer.addService(LIVEOAK, new ValueService<>(new ImmediateValue<>(system)))
                .install();

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

        ClientService client = new ClientService();
        serviceContainer.addService(CLIENT, client)
                .install();

        ClientConnectorService clientConnector = new ClientConnectorService();
        serviceContainer.addService(CLIENT.append( "connect" ), clientConnector )
                .addDependency( CLIENT, DefaultClient.class, clientConnector.clientInjector() )
                .addDependency(server("local", false))
                .install();

        ContainerService container = new ContainerService();
        ServiceBuilder<Container> containerBuilder = serviceContainer.addService(CONTAINER, container)
                .addDependency(DEPLOYER, Deployer.class, container.deployerInjector());

        if (configDir != null) {
            containerBuilder.addDependency(DEPLOYMENT_MANAGER, DirectoryDeploymentManager.class, container.deploymentManagerInjector());
        }

        containerBuilder.install();

        SubscriptionManagerService subscriptionManager = new SubscriptionManagerService();

        serviceContainer.addService(SUBSCRIPTION_MANAGER, subscriptionManager)
                .addDependency(CODEC_MANAGER, ResourceCodecManager.class, subscriptionManager.codecManagerInjector())
                .addInjection(subscriptionManager.idInjector(), "subscriptions")
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
                .addDependency(CONTAINER, Container.class, pipelineConfigurator.containerInjector())
                .addDependency(CODEC_MANAGER, ResourceCodecManager.class, pipelineConfigurator.codecManagerInjector())
                .addDependency(CLIENT, Client.class, pipelineConfigurator.clientInjector())
                .addDependency(WORKER_POOL, Executor.class, pipelineConfigurator.workerPoolInjector());

        if (configDir != null) {
            pipelineBuilder.addDependency(DEPLOYMENT_MANAGER, DirectoryDeploymentManager.class, pipelineConfigurator.deploymentManagerInjector());
        }

        pipelineBuilder.install();


        DeployerService deployer = new DeployerService();
        serviceContainer.addService(DEPLOYER, deployer)
                .install();

        DirectDeployerService directDeployer = new DirectDeployerService();
        serviceContainer.addService(DIRECT_DEPLOYER, directDeployer)
                .install();

        NotifierService notifier = new NotifierService();
        serviceContainer.addService(NOTIFIER, notifier)
                .addDependency(SUBSCRIPTION_MANAGER, SubscriptionManager.class, notifier.subscriptionManagerInjector())
                .install();

        if (configDir != null) {
            DeploymentManagerService deploymentManager = new DeploymentManagerService();
            serviceContainer.addService(DEPLOYMENT_MANAGER, deploymentManager)
                    .addDependency(DEPLOYER, Deployer.class, deploymentManager.deployerInjector())
                    .addInjection(deploymentManager.directoryInjector(), new File(configDir, "resources"))
                    .install();
        }


        if (vertx == null) {
            VertxService vertxSvc = new VertxService();
            serviceContainer.addService(VERTX, vertxSvc)
                    .addDependency(VERTX_PLATFORM_MANAGER, PlatformManager.class, vertxSvc.platformManagerInjector())
                    .install();

            serviceContainer.addService(VERTX_PLATFORM_MANAGER, new PlatformManagerService())
                    .install();
        } else {
            serviceContainer.addService(VERTX, new ValueService<>(new ImmediateValue<>(vertx)))
                    .install();
        }

        // ----------------------------------------
        // ----------------------------------------

        installInterceptor(serviceContainer, "timing", new TimingInterceptor());

        installCodec(serviceContainer, MediaType.JSON, JSONEncoder.class, new JSONDecoder());
        installCodec(serviceContainer, MediaType.HTML, HTMLEncoder.class, null);

        installRootResourceFactory(serviceContainer, new ClasspathBasedResourceFactory());
        installRootResourceFactory(serviceContainer, new ModuleBasedResourceFactory());

        installInternalResource(serviceContainer, LiveOak.SUBSCRIPTION_MANAGER);
        installInternalResource(serviceContainer, LiveOak.LIVEOAK);

        // ----------------------------------------
        // ----------------------------------------

        serviceContainer.awaitStability();

        return system;
    }

    private static void installInterceptor(ServiceContainer serviceContainer, String name, Interceptor interceptor) {
        ServiceName serviceName = interceptor(name);

        ServiceController<Interceptor> controller = serviceContainer.addService(serviceName, new ValueService<Interceptor>(new ImmediateValue<Interceptor>(interceptor)))
                .install();

        installInterceptor( serviceContainer, controller );
    }

    private static void installInterceptor(ServiceContainer serviceContainer, ServiceController<Interceptor> interceptor) {
        ServiceName serviceName = interceptor.getName();
        InterceptorRegistrationService registration = new InterceptorRegistrationService();
        serviceContainer.addService(serviceName.append("register"), registration)
                .addDependency(INTERCEPTOR_MANAGER, InterceptorManager.class, registration.interceptorManagerInjector())
                .addDependency(serviceName, Interceptor.class, registration.interceptorInjector())
                .install();
    }


    private static void installCodec(ServiceContainer serviceContainer, MediaType mediaType, Class<? extends Encoder> encoderClass, ResourceDecoder decoder) {
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

    static protected void installRootResourceFactory(ServiceContainer serviceContainer, RootResourceFactory factory) {
        ServiceName name = rootResourceFactory(factory.type());
        serviceContainer.addService(name, new ValueService<RootResourceFactory>(new ImmediateValue<RootResourceFactory>(factory)))
                .install();

        RootResourceFactoryRegistrationService registration = new RootResourceFactoryRegistrationService();
        serviceContainer.addService(name.append("register"), registration)
                .addDependency(name, RootResourceFactory.class, registration.factoryInjector())
                .addDependency(DEPLOYER, DefaultDeployer.class, registration.deployerInjector())
                .install();

    }

    static protected void installInternalResource(ServiceContainer serviceContainer, ServiceName resource) {
        InternalResourceDeploymentService service = new InternalResourceDeploymentService();

        serviceContainer.addService(resource.append("deploy"), service)
                .addDependency(resource, RootResource.class, service.resourceInjector())
                .addDependency(DIRECT_DEPLOYER, DirectDeployer.class, service.deployerInjector())
                .install();
    }
}
