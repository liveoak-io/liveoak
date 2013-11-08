package org.projectodd.restafari.container.subscriptions.resource;

import org.projectodd.restafari.container.aspects.ResourceAspect;
import org.projectodd.restafari.container.subscriptions.SubscriptionManager;
import org.projectodd.restafari.spi.resource.Resource;
import org.projectodd.restafari.spi.resource.async.SimplePropertyResource;

/**
 * @author Bob McWhirter
 */
public class SubscriptionsResourceAspect implements ResourceAspect {

    public SubscriptionsResourceAspect(SubscriptionManager subscriptionManager) {
        this.subscriptionManager = subscriptionManager;
    }

    @Override
    public Resource forResource(Resource resource) {
        if ( resource instanceof SubscriptionsCollectionResource ) {
            return null;
        }

        if ( resource instanceof SubscriptionResource ) {
            return null;
        }

        return new SubscriptionsCollectionResource( resource, this.subscriptionManager );
        //return new SimplePropertyResource( resource, "_subscriptions", new SubscriptionsCollectionResource( resource, this.subscriptionManager ) );
    }

    private SubscriptionManager subscriptionManager;
}
