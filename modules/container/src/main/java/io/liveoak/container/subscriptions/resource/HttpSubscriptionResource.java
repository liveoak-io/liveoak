package io.liveoak.container.subscriptions.resource;

import io.liveoak.common.codec.DefaultResourceState;
import io.liveoak.container.subscriptions.HttpSubscription;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.resource.SynchronousResource;
import io.liveoak.spi.resource.async.Resource;
import io.liveoak.spi.resource.async.Responder;
import io.liveoak.spi.state.ResourceState;

/**
 * @author Bob McWhirter
 */
public class HttpSubscriptionResource implements SynchronousResource {

    public HttpSubscriptionResource(ApplicationSubscriptionsResource parent, HttpSubscription subscription) {
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
        result.putProperty("type", "http");
        result.putProperty("path", this.subscription.resourcePath().toString());
        result.putProperty("destination", this.subscription.destination().toString());
        return result;
    }

    @Override
    public void delete(RequestContext ctx, Responder responder) throws Exception {
        this.parent.delete(this.subscription);
        responder.resourceDeleted(this);

    }

    private ApplicationSubscriptionsResource parent;
    private HttpSubscription subscription;
}
