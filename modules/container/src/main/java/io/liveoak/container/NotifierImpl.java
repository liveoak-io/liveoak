package io.liveoak.container;

import io.liveoak.container.subscriptions.DefaultSubscriptionManager;
import io.liveoak.spi.ResourceResponse;
import io.liveoak.spi.container.SubscriptionManager;
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
    public void resourceCreated(ResourceResponse resourceResponse) {
        this.subscriptionManager.resourceCreated(resourceResponse);
    }

    @Override
    public void resourceDeleted(ResourceResponse resourceResponse) {
        this.subscriptionManager.resourceDeleted(resourceResponse);
    }

    @Override
    public void resourceUpdated(ResourceResponse resourceResponse) {
        this.subscriptionManager.resourceUpdated(resourceResponse);
    }

    private SubscriptionManager subscriptionManager;
}
