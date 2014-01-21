package io.liveoak.container.subscriptions.resource;

import io.liveoak.container.subscriptions.StompSubscription;
import io.liveoak.spi.resource.SynchronousResource;
import io.liveoak.spi.resource.async.Resource;

/**
 * @author Bob McWhirter
 */
public class StompSubscriptionResource implements SynchronousResource {

    public StompSubscriptionResource(ApplicationSubscriptionsResource parent, StompSubscription subscription) {
        this.parent = parent;
        this.subscription = subscription;
    }

    @Override
    public Resource parent() {
        return this.parent;
    }

    @Override
    public String id() {
        return this.subscription.id();
    }

    private ApplicationSubscriptionsResource parent;
    private StompSubscription subscription;
}
