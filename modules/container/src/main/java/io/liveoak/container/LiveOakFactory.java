/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.container;

import java.io.File;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

import io.liveoak.container.service.bootstrap.ClientBootstrappingService;
import io.liveoak.container.service.bootstrap.CodecBootstrappingService;
import io.liveoak.container.service.bootstrap.ExtensionsBootstrappingService;
import io.liveoak.container.service.bootstrap.ServersBootstrappingService;
import io.liveoak.container.service.bootstrap.TenancyBootstrappingService;
import io.liveoak.container.service.bootstrap.VertxBootstrappingService;
import io.liveoak.container.tenancy.service.ApplicationsDirectoryPathDefaultValue;
import org.jboss.logging.Logger;
import org.jboss.msc.service.AbstractServiceListener;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.ServiceContainer;
import org.jboss.msc.service.ServiceController;
import org.jboss.msc.service.ServiceRegistry;
import org.jboss.msc.service.ServiceTarget;
import org.jboss.msc.service.StabilityMonitor;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.jboss.msc.service.ValueService;
import org.jboss.msc.value.DefaultValue;
import org.jboss.msc.value.ImmediateValue;
import org.vertx.java.core.Vertx;

import static io.liveoak.spi.LiveOak.LIVEOAK;
import static io.liveoak.spi.LiveOak.SERVICE_CONTAINER;
import static io.liveoak.spi.LiveOak.SERVICE_REGISTRY;
import static io.liveoak.spi.LiveOak.SOCKET_BINDING;
import static io.liveoak.spi.LiveOak.VERTX;

/**
 * Bootstrapping <code>main()</code> method.
 *
 * @author Bob McWhirter
 * @author Ken Finnigan
 */
public class LiveOakFactory {

    private static final Logger log = Logger.getLogger(LiveOakFactory.class);

    public static LiveOakSystem create() throws Exception {
        return create(null, null, null);
    }

    public static LiveOakSystem create(ServiceContainer serviceContainer, ServiceTarget serviceTarget) throws Exception {
        return new LiveOakFactory(null, null, null, null, serviceContainer, serviceTarget).createInternal();
    }

    public static LiveOakSystem create(Vertx vertx) throws Exception {
        return create(null, null, vertx);
    }

    public static LiveOakSystem create(File configDir, File applicationsDir) throws Exception {
        return create(configDir, applicationsDir, null);
    }

    public static LiveOakSystem create(File configDir, File applicationsDir, Vertx vertx) throws Exception {
        return new LiveOakFactory(configDir, applicationsDir, vertx, "localhost").createInternal();
    }

    public static LiveOakSystem create(File configDir, File applicationsDir, Vertx vertx, String bindAddress) throws Exception {
        return new LiveOakFactory(configDir, applicationsDir, vertx, bindAddress).createInternal();
    }

    // ----------------------------------------------------------------------
    // ----------------------------------------------------------------------

    private LiveOakFactory(File configDir, File applicationsDir, Vertx vertx, String bindAddress) {
        this(configDir, applicationsDir, vertx, bindAddress, ServiceContainer.Factory.create());
    }

    private LiveOakFactory(File configDir, File applicationsDir, Vertx vertx, String bindAddress, ServiceContainer serviceContainer) {
        this(configDir, applicationsDir, vertx, bindAddress, serviceContainer, serviceContainer.subTarget());
    }

    private LiveOakFactory(File configDir, File applicationsDir, Vertx vertx, String bindAddress, ServiceContainer serviceContainer, ServiceTarget serviceTarget) {
        this.configDir = configDir;
        this.appsDir = applicationsDir;
        this.vertx = vertx;
        this.bindAddress = bindAddress;
        this.serviceContainer = serviceContainer;
        this.serviceTarget = serviceTarget;

        this.serviceTarget.addListener(new AbstractServiceListener<Object>() {
            @Override
            public void transition(ServiceController<?> controller, ServiceController.Transition transition) {
                if (transition.getAfter().equals(ServiceController.Substate.START_FAILED)) {
                    log.errorf(controller.getStartException(), "Unable to start service: %s", controller.getName());
                }
            }
        });

        this.stabilityMonitor = new StabilityMonitor();
        this.serviceTarget.addMonitor(this.stabilityMonitor);
    }

    public LiveOakSystem createInternal() throws Exception {
        prolog();
        createTenancy();
        createServers();
        createClient();
        createExtensions();
        createVertx();
        this.stabilityMonitor.awaitStability();
        return (LiveOakSystem) serviceContainer.getService(LIVEOAK).awaitValue();
    }

    protected void prolog() {
        LiveOakSystem system = new LiveOakSystem(serviceContainer);
        serviceTarget.addService(LIVEOAK, new ValueService<>(new ImmediateValue<>(system)))
                .install();

        serviceTarget.addService(SERVICE_REGISTRY, new ValueService<>(new ImmediateValue<>(serviceContainer)))
                .install();

        serviceTarget.addService(SERVICE_CONTAINER, new ValueService<>(new ImmediateValue<>(serviceContainer)))
                .install();
    }

    protected void createTenancy() {
        serviceTarget.addService(LIVEOAK.append("apps-dir"),
                new ValueService<>(new DefaultValue<>(new ImmediateValue<>(this.appsDir != null ? this.appsDir.getAbsolutePath() : null), new ApplicationsDirectoryPathDefaultValue())))
                .install();

        TenancyBootstrappingService tenancy = new TenancyBootstrappingService();
        serviceTarget.addService(LIVEOAK.append("tenancy-bootstrap"), tenancy)
                .addDependency(LIVEOAK.append("apps-dir"), String.class, tenancy.applicationsDirectoryInjector())
                .install();
    }

    protected void createExtensions() {
        serviceTarget.addService(LIVEOAK.append("extn-dir"), new ValueService<>(new ImmediateValue<>(new File(configDir, "extensions").getAbsolutePath())))
                .install();

        ExtensionsBootstrappingService extensions = new ExtensionsBootstrappingService();
        serviceTarget.addService(LIVEOAK.append("extensions-bootstrap"), extensions)
                .addDependency(LIVEOAK.append("extn-dir"), String.class, extensions.extensionsDirectoryInjector())
                .install();
    }

    protected void createServers() throws UnknownHostException {
        serviceTarget.addService(SOCKET_BINDING, new Service<InetSocketAddress>() {
            private InetSocketAddress address;

            @Override
            public void start(StartContext context) throws StartException {
                address = new InetSocketAddress(bindAddress, 8080);
            }

            @Override
            public void stop(StopContext context) {
            }

            @Override
            public InetSocketAddress getValue() throws IllegalStateException, IllegalArgumentException {
                return address;
            }
        }).install();

        serviceTarget.addService(LIVEOAK.append("servers-bootstrap"), new ServersBootstrappingService()).install();
        serviceTarget.addService(LIVEOAK.append("codecs-bootstrap"), new CodecBootstrappingService()).install();
    }

    protected void createClient() {
        serviceTarget.addService(LIVEOAK.append("client-bootstrap"), new ClientBootstrappingService()).install();
    }

    protected void createVertx() {
        if (vertx == null) {
            serviceTarget.addService(LIVEOAK.append("vertx-bootstrap"), new VertxBootstrappingService())
                    .install();
        } else {
            serviceTarget.addService(VERTX, new ValueService<>(new ImmediateValue<>(vertx)))
                    .install();
        }
    }

    private final File configDir;
    private final File appsDir;
    private final Vertx vertx;
    private final String bindAddress;
    private final ServiceContainer serviceContainer;
    private final ServiceTarget serviceTarget;
    private final StabilityMonitor stabilityMonitor;

}
