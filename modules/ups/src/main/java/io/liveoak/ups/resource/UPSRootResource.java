package io.liveoak.ups.resource;

import io.liveoak.spi.RequestContext;
import io.liveoak.spi.container.SubscriptionManager;
import io.liveoak.spi.resource.RootResource;
import io.liveoak.spi.resource.async.Resource;
import io.liveoak.spi.resource.async.ResourceSink;
import io.liveoak.spi.resource.async.Responder;
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
    UPSSubscriptionsResource subscriptionsResource;

    UPS upsService;

    public UPSRootResource(String id, UPSRootConfigResource configResource, SubscriptionManager subscriptionManager) {
        this.id = id;
        this.configResource = configResource;
        this.subscriptionManager = subscriptionManager;

        //setup the service to handle communication with a UPS instance
        upsService = new UPS(configResource);

        this.aliasesResource = new AliasesResource( this, upsService, subscriptionManager );
        this.subscriptionsResource = new UPSSubscriptionsResource( this, upsService, subscriptionManager );
    }

    @Override
    public void parent( Resource parent ) {
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
    public void readMembers( RequestContext ctx, ResourceSink sink ) throws Exception {
        sink.accept(aliasesResource);
        sink.accept(subscriptionsResource);
        sink.close();
    }

    @Override
    public void readMember( RequestContext ctx, String id, Responder responder ) throws Exception {
        if (id.equals( AliasesResource.ID )) {
            responder.resourceRead( this.aliasesResource);
        } else if (id.equals( UPSSubscriptionsResource.ID)) {
            responder.resourceRead(this.subscriptionsResource);
        } else {
            responder.noSuchResource(id);
        }
    }

    public UPSRootConfigResource configuration() {
        return this.configResource;
    }
}
