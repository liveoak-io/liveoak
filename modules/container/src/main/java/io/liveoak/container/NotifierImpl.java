package io.liveoak.container;

import io.liveoak.container.subscriptions.SubscriptionManager;
import io.liveoak.spi.resource.async.Notifier;
import io.liveoak.spi.resource.async.Resource;

/**
 * @author Bob McWhirter
 */
public class NotifierImpl implements Notifier {

    public NotifierImpl(SubscriptionManager subscriptionManager) {
        this.subscriptionManager = subscriptionManager;
    }

    @Override
    public void resourceCreated(Resource resource) {
        this.subscriptionManager.resourceCreated(resource);
    }

    @Override
    public void resourceDeleted(Resource resource) {
        this.subscriptionManager.resourceDeleted(resource);
    }

    @Override
    public void resourceUpdated(Resource resource) {
        this.subscriptionManager.resourceUpdated(resource);
    }

    private SubscriptionManager subscriptionManager;
}
