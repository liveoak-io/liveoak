package io.liveoak.container.subscriptions;

import io.liveoak.spi.ResourcePath;
import io.liveoak.spi.ResourceResponse;
import io.liveoak.spi.container.Subscription;
import io.liveoak.spi.container.SubscriptionManager;
import io.liveoak.spi.resource.async.Resource;
import org.jboss.logging.Logger;

/**
 * @author Bob McWhirter
 */
public class DefaultSubscriptionManager implements SubscriptionManager {

    public DefaultSubscriptionManager() {

    }

    @Override
    public void addSubscription(Subscription subscription) {
        this.tree.addSubscription(subscription);
    }

    @Override
    public void removeSubscription(Subscription subscription) {
        this.tree.removeSubscription(subscription);
    }

    @Override
    public void resourceCreated(ResourceResponse resourceResponse) {
        ResourcePath path = resourcePathOf(resourceResponse.resource());
        this.tree.subscriptions(path).forEach((subscription) -> {
            subscribeResourceCreated(path, subscription, resourceResponse);
        });
    }

    protected void subscribeResourceCreated(ResourcePath path, Subscription subscription, ResourceResponse resourceResponse) {
        try {
            subscription.resourceCreated(resourceResponse);
        } catch (Exception e) {
            log.error("", e);
        }
    }

    @Override
    public void resourceUpdated(ResourceResponse resourceResponse) {
        ResourcePath path = resourcePathOf(resourceResponse.resource());
        this.tree.subscriptions(path).forEach((subscription) -> {
            subscribeResourceUpdated(path, subscription, resourceResponse);
        });
    }

    protected void subscribeResourceUpdated(ResourcePath path, Subscription subscription, ResourceResponse resourceResponse) {
        try {
            subscription.resourceUpdated(resourceResponse);
        } catch (Exception e) {
            log.error("", e);
        }
    }

    @Override
    public void resourceDeleted(ResourceResponse resourceResponse) {
        ResourcePath path = resourcePathOf(resourceResponse.resource());
        this.tree.subscriptions(path).forEach((subscription) -> {
            subscribeResourceDeleted(path, subscription, resourceResponse);
        });
    }

    protected void subscribeResourceDeleted(ResourcePath path, Subscription subscription, ResourceResponse resourceResponse) {
        try {
            subscription.resourceDeleted(resourceResponse);
        } catch (Exception e) {
            log.error("", e);
        }
    }

    protected ResourcePath resourcePathOf(Resource resource) {
        ResourcePath path = new ResourcePath();

        Resource current = resource;

        while (current != null) {
            if (!current.id().equals("")) {
                path.prependSegment(current.id());
            }
            current = current.parent();
        }

        return path;
    }

    public SubscriptionTree treeFor(ResourcePath path) {
        return this.tree.findLeaf(path);
    }

    private SubscriptionTree tree = new SubscriptionTree("");
    private static final Logger log = Logger.getLogger(DefaultSubscriptionManager.class);
}
