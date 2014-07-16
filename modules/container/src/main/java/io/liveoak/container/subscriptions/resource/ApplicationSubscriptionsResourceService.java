package io.liveoak.container.subscriptions.resource;

import io.liveoak.common.codec.ResourceCodecManager;
import io.liveoak.container.extension.MountService;
import io.liveoak.container.subscriptions.DefaultSubscriptionManager;
import io.liveoak.container.tenancy.InternalApplication;
import io.liveoak.container.tenancy.MountPointResource;
import io.liveoak.spi.LiveOak;
import org.jboss.msc.inject.Injector;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.ServiceTarget;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.jboss.msc.value.InjectedValue;
import org.vertx.java.core.Vertx;

/**
 * @author Bob McWhirter
 */
public class ApplicationSubscriptionsResourceService implements Service<ApplicationSubscriptionsResource> {


    public ApplicationSubscriptionsResourceService(InternalApplication app) {
        this.app = app;
    }

    @Override
    public void start(StartContext context) throws StartException {

        this.resource = new ApplicationSubscriptionsResource(
                this.subscriptionManagerInjector.getValue(),
                this.vertxInjector.getValue(),
                this.codecManagerInjector.getValue());

        MountService<ApplicationSubscriptionsResource> mount = new MountService<>();

        ServiceTarget target = context.getChildTarget();
        String appId = this.app.id();

        target.addService(context.getController().getName().append("mount"), mount)
                .addDependency(LiveOak.applicationContext(appId), MountPointResource.class, mount.mountPointInjector())
                .addInjectionValue(mount.resourceInjector(), this)
                .install();
    }


    @Override
    public void stop(StopContext context) {

    }

    @Override
    public ApplicationSubscriptionsResource getValue() throws IllegalStateException, IllegalArgumentException {
        return this.resource;
    }

    public Injector<DefaultSubscriptionManager> subscriptionManagerInjector() {
        return this.subscriptionManagerInjector;
    }

    public Injector<Vertx> vertxInjector() {
        return this.vertxInjector;
    }

    public Injector<ResourceCodecManager> codecManagerInjector() {
        return this.codecManagerInjector;
    }

    private final InternalApplication app;

    private InjectedValue<DefaultSubscriptionManager> subscriptionManagerInjector = new InjectedValue<>();
    private InjectedValue<Vertx> vertxInjector = new InjectedValue<>();
    private InjectedValue<ResourceCodecManager> codecManagerInjector = new InjectedValue<>();

    private ApplicationSubscriptionsResource resource;

}
