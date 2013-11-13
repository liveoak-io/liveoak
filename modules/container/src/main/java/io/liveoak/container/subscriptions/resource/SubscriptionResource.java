package io.liveoak.container.subscriptions.resource;

import io.liveoak.container.subscriptions.Subscription;
import io.liveoak.spi.resource.async.Resource;

/**
 * @author Bob McWhirter
 */
public class SubscriptionResource implements Resource {

    public SubscriptionResource(Resource parent, Subscription subscription) {
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

    private Resource parent;
    private Subscription subscription;
}
