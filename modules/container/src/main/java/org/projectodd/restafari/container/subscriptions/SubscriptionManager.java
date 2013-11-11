package org.projectodd.restafari.container.subscriptions;

import org.projectodd.restafari.spi.ResourcePath;
import org.projectodd.restafari.spi.resource.Resource;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * @author Bob McWhirter
 */
public class SubscriptionManager {

    public void resourceCreated(Resource resource) {
        getSubscriptions(resource).forEach((e) -> {
            try {
                e.resourceCreated(resource);
            } catch (Exception e1) {
                e1.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        });
    }

    public void resourceUpdated(Resource resource) {
        getSubscriptions(resource).forEach((e) -> {
            try {
                e.resourceUpdated(resource);
            } catch (Exception e1) {
                e1.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        });
    }

    public void resourceDeleted(Resource resource) {
        getSubscriptions(resource).forEach((e) -> {
            try {
                e.resourceDeleted(resource);
            } catch (Exception e1) {
                e1.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        });
    }

    public Stream<Subscription> getSubscriptions(Resource resource) {
        ResourcePath resourcePath = resourcePathOf(resource);
        return this.subscriptions.stream().filter((subscription) -> {
            ResourcePath subscriptionPath = subscription.resourcePath();
            return matches(subscriptionPath, resourcePath);
        });
    }

    public Subscription getSubscription(Resource resource, String id) {
        return getSubscriptions(resource).filter((e) -> e.id().equals(id)).findFirst().get();
    }

    protected boolean matches(ResourcePath subscriptionPath, ResourcePath resourcePath) {
        List<String> subscriptionSegments = subscriptionPath.segments();
        List<String> resourceSegments = resourcePath.segments();

        if (subscriptionSegments.size() > resourceSegments.size()) {
            return false;
        }

        int numSegments = subscriptionSegments.size();

        for (int i = 0; i < numSegments; ++i) {
            String subscriptionSegment = subscriptionSegments.get(i);
            if (subscriptionSegment.equals("*")) {
                continue;
            }
            String resourceSegment = resourceSegments.get(i);

            if (!subscriptionSegment.equals(resourceSegment)) {
                return false;
            }
        }

        return true;
    }

    protected ResourcePath resourcePathOf(Resource resource) {
        ResourcePath path = new ResourcePath();

        Resource current = resource;

        while (current != null) {
            path.prependSegment(current.id());
            current = current.parent();
        }

        return path;
    }

    public void addSubscription(Subscription subscription) {
        this.subscriptions.add(subscription);
    }

    private List<Subscription> subscriptions = new ArrayList<>();
}
