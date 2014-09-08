package io.liveoak.container.tenancy.service;

import io.liveoak.common.codec.ResourceCodecManager;
import io.liveoak.container.subscriptions.DefaultSubscriptionManager;
import io.liveoak.container.subscriptions.resource.ApplicationSubscriptionsResourceService;
import io.liveoak.container.tenancy.InternalApplication;
import io.liveoak.spi.Services;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.ServiceTarget;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.vertx.java.core.Vertx;

/**
 * @author Bob McWhirter
 */
public class ApplicationContextBootstrapService implements Service<Void> {

    public ApplicationContextBootstrapService(InternalApplication app) {
        this.app = app;
    }

    @Override
    public void start(StartContext context) throws StartException {
        ServiceTarget target = context.getChildTarget();
        ServiceName name = context.getController().getName();

        bootstrapSubscriptions(target, name.append("subscriptions"));
    }

    protected void bootstrapSubscriptions(ServiceTarget target, ServiceName name) {

        ApplicationSubscriptionsResourceService subscriptions = new ApplicationSubscriptionsResourceService(this.app);

        target.addService(name, subscriptions)
                .addDependency(Services.VERTX, Vertx.class, subscriptions.vertxInjector())
                .addDependency(Services.SUBSCRIPTION_MANAGER, DefaultSubscriptionManager.class, subscriptions.subscriptionManagerInjector())
                .addDependency(Services.CODEC_MANAGER, ResourceCodecManager.class, subscriptions.codecManagerInjector())
                .addDependency(Services.CODEC_MANAGER_COMPLETE)
                .install();
    }

    @Override
    public void stop(StopContext context) {

    }

    @Override
    public Void getValue() throws IllegalStateException, IllegalArgumentException {
        return null;
    }

    private final InternalApplication app;
}
