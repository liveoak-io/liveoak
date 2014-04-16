package io.liveoak.ups.resource;

import io.liveoak.spi.RequestContext;
import io.liveoak.spi.ResourcePath;
import io.liveoak.spi.ResourceResponse;
import io.liveoak.spi.resource.async.Responder;
import io.liveoak.spi.state.ResourceState;
import io.liveoak.ups.UPS;
import io.liveoak.ups.UPSSubscription;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 */
public class AliasSubscriptionResource extends SubscriptionResource {

    private AliasSubscriptionResource( AliasResource parent, UPS upsService, String id, ResourcePath resourcePath, UPSSubscription subscription ) {
        super( parent, upsService, id, subscription );
    }

    public static AliasSubscriptionResource create(AliasResource parent, UPS upsService, RequestContext ctx, ResourceState state, Responder responder) {
        Object pathProperty = state.getProperty(RESOURCE_PATH);
        if (pathProperty == null || !(pathProperty instanceof String)) {
            responder.invalidRequest("A String resource-path property is required when creating a SubscriptionResource.");
            return null;
        }

        // if the alias is specified, throw an error. The system handles aliases for this type of subscription
        if (state.getProperty( "alias" ) != null && !state.getProperty("alias").equals(parent.id)) {
            responder.invalidRequest( "Alias not allowed to be customized when creating this type of subscription." );
            return null;
        }

        String id = state.id();
        if (id == null) {
            id = UUID.randomUUID().toString();
        }

        List<String> aliases = new ArrayList<>();
        //NOTE: the alias is always the parent ID for this type of resource !
        aliases.add(parent.id);
        state.putProperty("alias", aliases);

        UPSSubscription subscription = generateSubscription(state);

        AliasSubscriptionResource subscriptionResource = new AliasSubscriptionResource(parent, upsService, id, subscription.resourcePath(), subscription);
        return subscriptionResource;
    }

    @Override
    public void delete(RequestContext ctx, Responder responder) throws Exception {
        ((AliasResource)parent).deleteSubscription( this.id() );
        responder.resourceDeleted(this);
    }

    @Override
    public void updateProperties(RequestContext ctx, ResourceState state, Responder responder) throws Exception {
        responder.updateNotSupported(this);
    }

    /** UPSSubscription Methods **/
    @Override
    public void resourceCreated( ResourceResponse resourceResponse ) throws Exception {
        upsService.send(resourceResponse.resource().uri(), UPS.EventType.CREATED, this.subscription);
    }

    @Override
    public void resourceUpdated( ResourceResponse resourceResponse ) throws Exception {
        upsService.send( resourceResponse.resource().uri(), UPS.EventType.UPDATED, this.subscription );
    }

    @Override
    public void resourceDeleted( ResourceResponse resourceResponse ) throws Exception {
        upsService.send( resourceResponse.resource().uri(), UPS.EventType.DELETED, this.subscription );
    }



}
