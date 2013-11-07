package org.projectodd.restafari.container.subscriptions.resource;

import org.projectodd.restafari.container.subscriptions.Subscription;
import org.projectodd.restafari.spi.RequestContext;
import org.projectodd.restafari.spi.resource.Resource;
import org.projectodd.restafari.spi.resource.async.ObjectResource;
import org.projectodd.restafari.spi.resource.async.ResourceSink;
import org.projectodd.restafari.spi.resource.async.Responder;
import org.projectodd.restafari.spi.state.ObjectResourceState;

/**
 * @author Bob McWhirter
 */
public class SubscriptionResource implements ObjectResource {

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

    @Override
    public void readMember(RequestContext ctx, String id, Responder responder) {
        responder.noSuchResource( id );
    }

    @Override
    public void delete(RequestContext ctx, Responder responder) {
        responder.deleteNotSupported( this );
    }


    @Override
    public void update(RequestContext ctx, ObjectResourceState state, Responder responder) {
        responder.updateNotSupported( this );
    }

    @Override
    public void readMembers(RequestContext ctx, ResourceSink sink) {
        sink.close();
    }

    private Resource parent;
    private Subscription subscription;
}
