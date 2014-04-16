package io.liveoak.ups.resource;

import io.liveoak.spi.RequestContext;
import io.liveoak.spi.ResourcePath;
import io.liveoak.spi.ResourceResponse;
import io.liveoak.spi.SecurityContext;
import io.liveoak.spi.container.Subscription;
import io.liveoak.spi.resource.async.PropertySink;
import io.liveoak.spi.resource.async.Resource;
import io.liveoak.spi.resource.async.ResourceSink;
import io.liveoak.spi.resource.async.Responder;
import io.liveoak.spi.state.ResourceState;
import io.liveoak.ups.UPS;
import io.liveoak.ups.UPSSubscription;
import org.jboss.logging.Logger;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 */
public class SubscriptionResource implements Subscription, Resource {

    private static final Logger log = Logger.getLogger("io.liveoak.ups");

    // LiveOAK specifics
    static final String RESOURCE_PATH = "resource-path";

    // UPS specifics
    // try and match the names as closely as possible as to what is normally sent to UPS
    static final String VARIANTS = "variants";
    static final String ALIASES = "alias";
    static final String CATEGORIES = "categories";
    static final String DEVICES = "device-type";
    static final String SIMPLE_PUSH = "simple-push";
    static final String MESSAGE = "message";

    String id;
    Resource parent;
    UPS upsService;
    UPSSubscription subscription;
    //ResourcePath resourcePath;

    protected SubscriptionResource(Resource parent, UPS upsService, String id, UPSSubscription subscription ) {
        this.parent = parent;
        this.id = id;
        this.upsService = upsService;
        this.subscription = subscription;
        //this.resourcePath = resourcePath;
    }

    public static SubscriptionResource create(Resource parent, UPS upsService, RequestContext ctx, ResourceState state, Responder responder) {
        Object pathProperty = state.getProperty(RESOURCE_PATH);
        if (pathProperty == null || !(pathProperty instanceof String)) {
            responder.invalidRequest("A String resource-path property is required when creating a SubscriptionResource.");
            return null;
        }

        String id = state.id();
        if (id == null) {
            id = UUID.randomUUID().toString();
        }

        UPSSubscription subscription = generateSubscription(state);

        SubscriptionResource subscriptionResource = new SubscriptionResource(parent, upsService, id, subscription);
        return subscriptionResource;
    }

    protected static UPSSubscription generateSubscription(ResourceState state) {

        String path = (String) state.getProperty(RESOURCE_PATH);

        Object idProperty = state.getProperty("id");
        String id;
        if (idProperty == null) {
            id = UUID.randomUUID().toString();
        } else {
            id = idProperty.toString();
        }

        UPSSubscription subscription = new UPSSubscription(path);

        List<String> variants = (List) state.getProperty( VARIANTS );
        if (variants != null) {
            subscription.variants(variants);
        }

        List<String> aliases = (List) state.getProperty( ALIASES );
        if (aliases != null) {
            subscription.aliases(aliases);
        }

        List<String> categories = (List) state.getProperty( CATEGORIES );
        if (categories != null) {
            subscription.categories(new HashSet(categories));
        }

        List<String> deviceTypes = (List) state.getProperty(DEVICES);
        if (deviceTypes != null) {
            subscription.deviceTypes(deviceTypes);
        }

        Integer simplePush = (Integer) state.getProperty( SIMPLE_PUSH );
        if (simplePush != null) {
            subscription.simplePush(simplePush);
        }

        ResourceState message = (ResourceState) state.getProperty(MESSAGE);
        if (message != null) {
            Map<String, Object> attributes = new HashMap();
            for (String property : message.getPropertyNames()) {
                attributes.put( property, message.getProperty(property));
            }
            subscription.setMessage( attributes );
        }

        return subscription;
    }


    @Override
    public Resource parent() {
        return parent;
    }

    @Override
    public String id() {
        return id;
    }

    @Override
    public ResourcePath resourcePath() {
        return this.subscription.resourcePath();
    }

    @Override
    public boolean isSecure() {
        // Right now, UPS is sending just resource URI and not whole resources, so it's not secured
        return false;
    }

    @Override
    public SecurityContext securityContext() {
        // Using anonymous for now
        return SecurityContext.ANONYMOUS;
    }

    @Override
    public void sendAuthzError(ResourceState errorState, Resource resource, int status) throws Exception {
        throw new IllegalStateException("Authorization error not expected for SubscriptionResource");
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

    /** Resource Methods **/

    @Override
    public void readProperties(RequestContext ctx, PropertySink sink) throws Exception {
        sink.accept(this.RESOURCE_PATH, this.subscription.resourcePath().toString());
        sink.accept(this.VARIANTS, subscription.variants());
        sink.accept(this.ALIASES ,subscription.aliases());
        sink.accept(this.CATEGORIES, subscription.categories());
        sink.accept(this.DEVICES, subscription.deviceTypes());
        sink.accept(this.SIMPLE_PUSH, subscription.simplePush());
        sink.accept(UPSMessageResource.ID, new UPSMessageResource(this, subscription.message()));
        sink.close();
    }

    @Override
    public void updateProperties(RequestContext ctx, ResourceState state, Responder responder) throws Exception {
        Object pathProperty = state.getProperty(RESOURCE_PATH);
        if (pathProperty == null || !(pathProperty instanceof String)) {
            responder.invalidRequest("A String resource-path property is required when creating a SubscriptionResource.");
            return;
        }

        this.subscription = generateSubscription(state);

        responder.resourceUpdated( this );
    }

    @Override
    public void readMembers(RequestContext ctx, ResourceSink sink) throws Exception {
        sink.close();
    }

    @Override
    public void readMember(RequestContext ctx, String id, Responder responder) throws Exception {
        responder.noSuchResource(id);
    }

    @Override
    public void delete(RequestContext ctx, Responder responder) throws Exception {
        ((UPSSubscriptionsResource)this.parent).deleteSubscription( this );
        responder.resourceDeleted(this);
    }

}
