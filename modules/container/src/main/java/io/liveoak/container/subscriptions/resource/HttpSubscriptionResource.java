package io.liveoak.container.subscriptions.resource;

import io.liveoak.container.subscriptions.HttpSubscription;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.resource.SynchronousResource;
import io.liveoak.spi.resource.async.PropertySink;
import io.liveoak.spi.resource.async.Resource;
import io.liveoak.spi.resource.async.Responder;

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
    public void readProperties(RequestContext ctx, PropertySink sink) throws Exception {
        sink.accept("type", "http");
        sink.accept("path", this.subscription.resourcePath().toString());
        sink.accept("destination", this.subscription.destination().toString());
        sink.close();
    }

    @Override
    public void delete(RequestContext ctx, Responder responder) throws Exception {
        this.parent.delete(this.subscription);
        responder.resourceDeleted(this);

    }

    private ApplicationSubscriptionsResource parent;
    private HttpSubscription subscription;
}
