package org.projectodd.restafari.container.subscriptions;

import org.projectodd.restafari.container.ResourcePath;
import org.projectodd.restafari.spi.Resource;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

/**
 * @author Bob McWhirter
 */
public class SubscriptionManager {

    public void resourceCreated(String type, String collectionName, Resource resource) {
        getSubscriptions(type, collectionName, resource.getId()).forEach((e) -> {
            e.resourceUpdated(resource);
        });
    }

    public void resourceUpdated(String type, String collectionName, Resource resource) {
        getSubscriptions(type, collectionName, resource.getId()).forEach((e) -> {
            e.resourceUpdated(resource);
        });
    }

    public void resourceDeleted(String type, String collectionName, Resource resource) {
        getSubscriptions(type, collectionName, resource.getId()).forEach((e) -> {
            e.resourceDeleted(resource);
        });
    }

    protected Stream<Subscription> getSubscriptions(String type, String collectionName, String resourceId) {
        return this.subscriptions.stream().filter((e) -> {
            ResourcePath p = e.resourcePath();
            return (p.getType().equals(type) && p.getCollectionName().equals(collectionName)) &&
                    (p.isCollectionPath() || (p.isResourcePath() && p.getResourceId().equals(resourceId)));
        });
    }

    public void addSubscription(Subscription subscription) {
        this.subscriptions.add(subscription);
    }

    private List<Subscription> subscriptions = new ArrayList<>();
}
