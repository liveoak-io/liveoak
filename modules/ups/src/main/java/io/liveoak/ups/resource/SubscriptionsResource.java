package io.liveoak.ups.resource;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.WriteResult;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.resource.async.Resource;
import io.liveoak.spi.resource.async.ResourceSink;
import io.liveoak.spi.resource.async.Responder;
import io.liveoak.spi.state.ResourceState;
import io.liveoak.ups.UPSSubscription;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 */
public class SubscriptionsResource implements SubscriptionResourceParent {

    public static final String ID = "subscriptions";

    protected UPSRootResource parent;
    protected DBCollection collection;

    public SubscriptionsResource(UPSRootResource parent, DBCollection collection) {
        this.parent = parent;
        this.collection = collection;
    }

    @Override
    public Resource parent() {
        return parent;
    }

    @Override
    public String id() {
        return ID;
    }

    @Override
    public void readMembers(RequestContext ctx, ResourceSink sink) throws Exception {
        DBCursor cursor = collection.find();
        while (cursor.hasNext()) {
            DBObject dbObject = cursor.next();
            UPSSubscription subscription = UPSSubscription.create(dbObject);
            if (subscription != null) {
                SubscriptionResource subscriptionResource = new SubscriptionResource(this, subscription);
                sink.accept(subscriptionResource);
            }
        }
        sink.close();
    }

    @Override
    public void readMember(RequestContext ctx, String id, Responder responder) throws Exception {
        DBObject dbObject = collection.findOne(new BasicDBObject("_id", id));
        if (dbObject != null) {
            UPSSubscription subscription = UPSSubscription.create(dbObject);
            if (subscription != null) {
                SubscriptionResource subscriptionResource = new SubscriptionResource(this, subscription);
                responder.resourceRead(subscriptionResource);
                return;
            }
        }

        responder.noSuchResource(id);
    }

    @Override
    public void createMember(RequestContext ctx, ResourceState state, Responder responder) throws Exception {
        UPSSubscription subscription = UPSSubscription.create(state);
        if (subscription != null) {
            this.collection.insert(subscription.dbObject());
            responder.resourceCreated(new SubscriptionResource(this, subscription));
        } else {
            responder.invalidRequest("Cannot create a UPS Subscription without a resource-path specified");
        }
    }

    public void updateSubscription(UPSSubscription subscription) {
        String id = subscription.id();
        WriteResult wr = this.collection.save(subscription.dbObject());
    }

    public void deleteSubscription(String id) {
        collection.remove(new BasicDBObject("_id", id));
    }
}
