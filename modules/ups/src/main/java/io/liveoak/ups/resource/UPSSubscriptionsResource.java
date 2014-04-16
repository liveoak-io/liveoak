package io.liveoak.ups.resource;

import io.liveoak.spi.RequestContext;
import io.liveoak.spi.container.SubscriptionManager;
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
public class UPSSubscriptionsResource implements Resource {

    UPSRootResource parent;
    SubscriptionManager subscriptionManager;
    UPS upsService;
    public static final String ID = "subscriptions";

    Map<String, SubscriptionResource> subscriptions = new HashMap<>();

    public UPSSubscriptionsResource (UPSRootResource parent, UPS upsService, SubscriptionManager subscriptionManager) {
        this.parent = parent;
        this.subscriptionManager = subscriptionManager;
        this.upsService = upsService;
    }

    @Override
    public Resource parent() {
        return this.parent;
    }

    @Override
    public String id() {
        return ID;
    }

    @Override
    public void readMembers(RequestContext ctx, ResourceSink sink) throws Exception {
        for (SubscriptionResource subscription : subscriptions.values()){
            sink.accept(subscription);
        }
        sink.close();
    }

    @Override
    public void readMember(RequestContext ctx, String id, Responder responder) throws Exception {
        if (subscriptions.containsKey( id )) {
           responder.resourceRead( subscriptions.get( id ) );
        } else {
            responder.noSuchResource( id );
        }
    }

    @Override
    public void createMember(RequestContext ctx, ResourceState state, Responder responder) throws Exception {
        SubscriptionResource subscription =  SubscriptionResource.create( this, upsService, ctx, state, responder );

        if (subscription != null) {
            subscriptions.put(subscription.id(),subscription) ;
            subscriptionManager.addSubscription(subscription);
            responder.resourceCreated(subscription);
        }
    }

    public void deleteSubscription(SubscriptionResource subscription) {
        subscriptionManager.removeSubscription( subscription );
        subscriptions.remove(subscription.id());
    }

    public void updateSubscription(SubscriptionResource subscription) {
        subscriptionManager.removeSubscription( subscription );
        subscriptionManager.addSubscription(subscription);
        subscriptions.put(subscription.id(), subscription);
    }

}
