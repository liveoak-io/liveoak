package io.liveoak.container.subscriptions.resource;

import io.liveoak.common.codec.DefaultResourceState;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.container.Subscription;
import io.liveoak.spi.resource.SynchronousResource;
import io.liveoak.spi.resource.async.Resource;
import io.liveoak.spi.state.ResourceState;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 */
public class BasicSubscriptionResource implements SynchronousResource {

    public BasicSubscriptionResource(ApplicationSubscriptionsResource parent, Subscription subscription) {
        this.parent = parent;
        this.subscription = subscription;
    }

    @Override
    public Resource parent() {
        return this.parent;
    }

    @Override
    public String id() {
        return this.subscription.id();
    }

    @Override
    public ResourceState properties(RequestContext ctx) throws Exception {
        ResourceState result = new DefaultResourceState();
        result.putProperty("resourcepath", subscription.resourcePath().toString());
        return result;
    }

    private ApplicationSubscriptionsResource parent;
    private Subscription subscription;
}
