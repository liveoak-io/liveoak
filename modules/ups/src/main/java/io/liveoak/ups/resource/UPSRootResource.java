package io.liveoak.ups.resource;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import io.liveoak.mongo.internal.InternalStorage;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.container.SubscriptionManager;
import io.liveoak.spi.resource.RootResource;
import io.liveoak.spi.resource.async.Resource;
import io.liveoak.spi.resource.async.ResourceSink;
import io.liveoak.spi.resource.async.Responder;
import io.liveoak.ups.AliasUPSSubscription;
import io.liveoak.ups.BaseUPSSubscription;
import io.liveoak.ups.UPS;
import io.liveoak.ups.resource.config.UPSRootConfigResource;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 */
public class UPSRootResource implements RootResource {

    Resource parent;
    String id;
    UPSRootConfigResource configResource;
    SubscriptionManager subscriptionManager;
    AliasesResource aliasesResource;
    SubscriptionsResource subscriptionsResource;
    InternalStorage internalStorage;

    UPS upsService;

    public UPSRootResource(String id, UPSRootConfigResource configResource, SubscriptionManager subscriptionManager, InternalStorage internalStorage) {
        this.id = id;
        this.configResource = configResource;
        this.subscriptionManager = subscriptionManager;
        this.internalStorage = internalStorage;

        //setup the service to handle communication with a UPS instance
        upsService = new UPS(configResource);

        DBCollection subscriptionsCollections = internalStorage.getCollection("subscriptions");
        subscriptionsCollections.ensureIndex(new BasicDBObject("resource-path", 1));
        subscriptionManager.addSubscription(new BaseUPSSubscription(subscriptionsCollections, upsService));

        this.subscriptionsResource = new SubscriptionsResource(this, subscriptionsCollections);

        DBCollection aliasesCollection = internalStorage.getCollection("aliases");
        // adds the index if it doesn't already exist
        aliasesCollection.ensureIndex(new BasicDBObject("subscriptions.resource-path", 1));

        subscriptionManager.addSubscription(new AliasUPSSubscription(aliasesCollection, upsService));

        this.aliasesResource = new AliasesResource(this, aliasesCollection);

    }

    @Override
    public void parent(Resource parent) {
        this.parent = parent;
    }

    @Override
    public Resource parent() {
        return this.parent;
    }

    @Override
    public String id() {
        return this.id;
    }

    @Override
    public void readMembers(RequestContext ctx, ResourceSink sink) throws Exception {
        try {
            sink.accept(aliasesResource);
            sink.accept(subscriptionsResource);
        } catch (Throwable e) {
            sink.error(e);
        } finally {
            sink.complete();
        }
    }

    @Override
    public void readMember(RequestContext ctx, String id, Responder responder) throws Exception {
        if (id.equals(AliasesResource.ID)) {
            responder.resourceRead(this.aliasesResource);
        } else if (id.equals(SubscriptionsResource.ID)) {
            responder.resourceRead(this.subscriptionsResource);
        } else {
            responder.noSuchResource(id);
        }
    }

    public UPSRootConfigResource configuration() {
        return this.configResource;
    }
}
