package io.liveoak.container.subscriptions.resource;

import io.liveoak.spi.RequestContext;
import io.liveoak.spi.container.Subscription;
import io.liveoak.spi.resource.SynchronousResource;
import io.liveoak.spi.resource.async.PropertySink;
import io.liveoak.spi.resource.async.Resource;

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
    public void readProperties(RequestContext ctx, PropertySink sink) throws Exception {
        sink.accept("resourcepath", subscription.resourcePath().toString());
        sink.close();
    }

    private ApplicationSubscriptionsResource parent;
    private Subscription subscription;
}
