package io.liveoak.ups.resource;

import io.liveoak.spi.RequestContext;
import io.liveoak.spi.container.SubscriptionManager;
import io.liveoak.spi.resource.async.Resource;
import io.liveoak.spi.resource.async.ResourceSink;
import io.liveoak.spi.resource.async.Responder;
import io.liveoak.spi.state.ResourceState;
import io.liveoak.ups.resource.config.UPSRootConfigResource;

import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 */
public class UPSSubscriptionsResource implements Resource {

    UPSRootResource parent;
    UPSRootConfigResource config;
    SubscriptionManager subscriptionManager;
    public static final String ID = "subscriptions";

    Map<String, UPSSubscription> subscriptions = new HashMap<>();

    public UPSSubscriptionsResource (UPSRootResource parent, UPSRootConfigResource config,  SubscriptionManager subscriptionManager) {
        this.parent = parent;
        this.subscriptionManager = subscriptionManager;
        this.config = config;
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
        for (UPSSubscription subscription : subscriptions.values()){
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
        UPSSubscription subscription = UPSSubscription.create(this, config, ctx, state, responder);

        if (subscription != null) {
            subscriptions.put(subscription.id(),subscription) ;
            subscriptionManager.addSubscription(subscription);
        }

        responder.resourceCreated(subscription);
    }

    public void deleteSubscription(UPSSubscription subscription) {
        subscriptionManager.removeSubscription( subscription );
        subscriptions.remove(subscription.id());
    }

    public void updateSubscription(UPSSubscription subscription) {
        subscriptionManager.removeSubscription( subscription );
        subscriptionManager.addSubscription(subscription);
        subscriptions.put(subscription.id(), subscription);
    }


}
