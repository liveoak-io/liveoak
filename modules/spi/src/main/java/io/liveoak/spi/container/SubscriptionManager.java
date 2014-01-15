package io.liveoak.spi.container;

import io.liveoak.spi.ResourceResponse;
import io.liveoak.spi.resource.RootResource;
import io.liveoak.spi.resource.async.Resource;

/**
 * @author Bob McWhirter
 */
public interface SubscriptionManager extends RootResource {
    void addSubscription(Subscription subscription);

    void resourceCreated(ResourceResponse resourceResponse);

    void resourceUpdated(ResourceResponse resourceResponse);

    void resourceDeleted(ResourceResponse resourceResponse);
}
