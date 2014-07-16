package io.liveoak.spi.container;

import io.liveoak.spi.ResourceResponse;

/**
 * @author Bob McWhirter
 */
public interface SubscriptionManager {

    void addSubscription(Subscription subscription);

    void removeSubscription(Subscription subscription);

    void resourceCreated(ResourceResponse resourceResponse);

    void resourceUpdated(ResourceResponse resourceResponse);

    void resourceDeleted(ResourceResponse resourceResponse);
}
