package io.liveoak.spi.container;

import io.liveoak.spi.ResourceResponse;

/**
 * @author Bob McWhirter
 * @author Ken Finnigan
 */
public interface SubscriptionManager {

    void addSubscription(Subscription subscription);

    void removeSubscription(Subscription subscription);

    Subscription getSubscription(String subscriptionId);

    void resourceCreated(ResourceResponse resourceResponse);

    void resourceUpdated(ResourceResponse resourceResponse);

    void resourceDeleted(ResourceResponse resourceResponse);
}
