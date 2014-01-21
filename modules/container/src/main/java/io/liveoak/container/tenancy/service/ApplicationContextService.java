package io.liveoak.container.tenancy.service;

import io.liveoak.common.codec.ResourceCodecManager;
import io.liveoak.container.extension.MountService;
import io.liveoak.container.subscriptions.DefaultSubscriptionManager;
import io.liveoak.container.subscriptions.resource.ApplicationSubscriptionsResource;
import io.liveoak.container.subscriptions.resource.ApplicationSubscriptionsResourceService;
import io.liveoak.container.tenancy.ApplicationContext;
import io.liveoak.container.tenancy.InternalApplication;
import io.liveoak.container.tenancy.MountPointResource;
import io.liveoak.spi.LiveOak;
import org.jboss.msc.service.*;
import org.vertx.java.core.Vertx;

/**
 * @author Bob McWhirter
 */
public class ApplicationContextService implements Service<ApplicationContext> {

    public ApplicationContextService(InternalApplication app) {
        this.app = app;
    }

    @Override
    public void start(StartContext context) throws StartException {
        this.context = new ApplicationContext(this.app.id());

        ServiceTarget target = context.getChildTarget();
        ServiceName name = context.getController().getName();

        MountService<ApplicationContext> mount = new MountService<>();

        target.addService(name.append("mount"), mount)
                .addInjectionValue(mount.resourceInjector(), this)
                .addDependency(LiveOak.organizationContext(this.app.organization().id()), MountPointResource.class, mount.mountPointInjector())
                .install();

        target.addService(name.append("bootstrap"), new ApplicationContextBootstrapService(this.app))
                .install();
    }

    @Override
    public void stop(StopContext context) {
        this.context = null;
    }

    @Override
    public ApplicationContext getValue() throws IllegalStateException, IllegalArgumentException {
        return this.context;
    }

    private InternalApplication app;
    private ApplicationContext context;
}
