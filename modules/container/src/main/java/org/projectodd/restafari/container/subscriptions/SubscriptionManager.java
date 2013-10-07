package org.projectodd.restafari.container.subscriptions;

import org.projectodd.restafari.spi.Resource;

import java.util.Collection;
import java.util.Collections;

/**
 * @author Bob McWhirter
 */
public class SubscriptionManager {

    public void resourceCreated(String type, Resource resource) {
        Collection<Subscription> subscriptions = findSubscriptions( type );
        for ( Subscription each : subscriptions ) {
            each.resourceCreated( resource );
        }
    }

    public void resourceUpdated(String type, Resource resource) {
        Collection<Subscription> subscriptions = findSubscriptions( type, resource.getId() );
        for ( Subscription each : subscriptions ) {
            each.resourceUpdated( resource );
        }
    }

    public void resourceDeleted(String type, Resource resource) {
        Collection<Subscription> subscriptions = findSubscriptions( type, resource.getId() );
        for ( Subscription each : subscriptions ) {
            each.resourceDeleted( resource );
        }
    }

    protected Collection<Subscription> findSubscriptions(String type) {
        // find subscriptions for the collection type
        return Collections.emptyList();
    }

    protected Collection<Subscription> findSubscriptions(String type, String id) {
        // find subscriptions for the collection type, or the particular resource
        return Collections.emptyList();
    }
}
