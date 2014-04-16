package io.liveoak.ups.resource;

import io.liveoak.spi.RequestContext;
import io.liveoak.spi.resource.async.PropertySink;
import io.liveoak.spi.resource.async.Resource;
import io.liveoak.spi.resource.async.ResourceSink;
import io.liveoak.spi.resource.async.Responder;
import io.liveoak.spi.state.ResourceState;
import io.liveoak.ups.UPS;

import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 */
public class AliasResource implements Resource {

    AliasesResource parent;
    String id;
    String userid;
    UPS upsService;

    Map<String, SubscriptionResource> resources;

    public AliasResource(AliasesResource parent, UPS upsService, String id, String userid) {
        this.parent = parent;
        this.id = id;
        this.userid = userid;
        this.resources = new HashMap<String, SubscriptionResource>();
        this.upsService = upsService;
    }

    @Override
    public Resource parent() {
        return parent;
    }

    @Override
    public String id() {
        return this.id;
    }

    @Override
    public void readProperties(RequestContext ctx, PropertySink sink) throws Exception {
        sink.accept("user", this.userid );
        sink.close();
    }

    @Override
    public void readMembers(RequestContext ctx, ResourceSink sink) throws Exception {
        for (SubscriptionResource subscriptionResource: resources.values()) {
            sink.accept(subscriptionResource);
        }
        sink.close();
    }

    @Override
    public void readMember(RequestContext ctx, String id, Responder responder) throws Exception {
        SubscriptionResource subscription = resources.get( id );
        if (subscription != null) {
            responder.resourceRead(subscription);
        } else {
            responder.noSuchResource(id);
        }
    }

    @Override
    public void createMember(RequestContext ctx, ResourceState state, Responder responder) throws Exception {
        SubscriptionResource subscriptionResource = AliasSubscriptionResource.create(this, this.upsService, ctx, state, responder );
        if (subscriptionResource != null) {
            resources.put(subscriptionResource.id(), subscriptionResource);
            parent.getSubscriptionManager().addSubscription(subscriptionResource);
            responder.resourceCreated(subscriptionResource);
        }
    }

    @Override
    public void delete(RequestContext ctx, Responder responder) throws Exception {
        parent.removeAlias(this.id, ctx, responder);
        responder.resourceDeleted(this);
    }

    public void deleteSubscription(String id) {
        parent.getSubscriptionManager().removeSubscription( resources.get(id) );
        resources.remove(id);
    }
}
