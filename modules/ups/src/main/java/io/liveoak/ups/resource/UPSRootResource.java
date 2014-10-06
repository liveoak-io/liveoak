package io.liveoak.ups.resource;

import java.util.Collection;
import java.util.LinkedList;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import io.liveoak.mongo.internal.InternalStorage;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.container.SubscriptionManager;
import io.liveoak.spi.resource.RootResource;
import io.liveoak.spi.resource.SynchronousResource;
import io.liveoak.spi.resource.async.Resource;
import io.liveoak.ups.AliasUPSSubscription;
import io.liveoak.ups.BaseUPSSubscription;
import io.liveoak.ups.UPS;
import io.liveoak.ups.resource.config.UPSRootConfigResource;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 */
public class UPSRootResource implements RootResource, SynchronousResource {

    private Resource parent;
    private String id;
    private UPSRootConfigResource configResource;
    private SubscriptionManager subscriptionManager;
    private AliasesResource aliasesResource;
    private SubscriptionsResource subscriptionsResource;
    private InternalStorage internalStorage;

    private UPS upsService;

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
    public Collection<Resource> members(RequestContext ctx) throws Exception {
        LinkedList<Resource> members = new LinkedList<>();
        members.add(aliasesResource);
        members.add(subscriptionsResource);
        return members;
    }

    @Override
    public Resource member(RequestContext ctx, String id) throws Exception {
        if (id.equals(AliasesResource.ID)) {
            return this.aliasesResource;
        } else if (id.equals(SubscriptionsResource.ID)) {
            return this.subscriptionsResource;
        }
        return null;
    }

    public UPSRootConfigResource configuration() {
        return this.configResource;
    }
}
