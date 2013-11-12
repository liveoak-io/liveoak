package org.projectodd.restafari.container.subscriptions.resource;

import org.projectodd.restafari.container.subscriptions.Subscription;
import org.projectodd.restafari.spi.RequestContext;
import org.projectodd.restafari.spi.resource.async.Resource;
import org.projectodd.restafari.spi.resource.async.ResourceSink;
import org.projectodd.restafari.spi.resource.async.Responder;

/**
 * @author Bob McWhirter
 */
public class SubscriptionResource implements Resource {

    public SubscriptionResource(Resource parent, Subscription subscription) {
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

    private Resource parent;
    private Subscription subscription;
}
