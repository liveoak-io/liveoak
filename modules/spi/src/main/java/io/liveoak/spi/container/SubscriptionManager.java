package io.liveoak.spi.container;

import io.liveoak.spi.resource.RootResource;
import io.liveoak.spi.resource.async.Resource;

/**
 * @author Bob McWhirter
 */
public interface SubscriptionManager extends RootResource {
    void addSubscription(Subscription subscription);

    void resourceCreated(Resource resource);

    void resourceUpdated(Resource resource);

    void resourceDeleted(Resource resource);
}
