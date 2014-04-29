package io.liveoak.ups.resource;

import io.liveoak.spi.RequestContext;
import io.liveoak.spi.resource.async.PropertySink;
import io.liveoak.spi.resource.async.Resource;
import io.liveoak.spi.resource.async.Responder;
import io.liveoak.spi.state.ResourceState;
import io.liveoak.ups.UPSSubscription;
import org.jboss.logging.Logger;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 */
public class SubscriptionResource implements Resource {

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

    static final String ENABLED = "enabled";

    protected SubscriptionResourceParent parent;
    protected UPSSubscription subscription;

    public SubscriptionResource(SubscriptionResourceParent parent, UPSSubscription subscription) {
        this.parent = parent;
        this.subscription = subscription;
    }

    @Override
    public Resource parent() {
        return parent;
    }

    @Override
    public String id() {
        return subscription.id();
    }

    @Override
    public void readProperties(RequestContext ctx, PropertySink sink) throws Exception {
        sink.accept(this.RESOURCE_PATH, this.subscription.resourcePath().toString());
        sink.accept(this.VARIANTS, subscription.variants());
        sink.accept(this.ALIASES ,subscription.aliases());
        sink.accept(this.CATEGORIES, subscription.categories());
        sink.accept(this.DEVICES, subscription.deviceTypes());
        sink.accept(this.SIMPLE_PUSH, subscription.simplePush());
        sink.accept(this.MESSAGE, new UPSMessageResource(this, subscription.message()));
        sink.accept(this.ENABLED, subscription.enabled());
        sink.close();
    }

    @Override
    public void updateProperties(RequestContext ctx, ResourceState state, Responder responder) throws Exception {
        state.id(this.id());
        UPSSubscription subscription = UPSSubscription.create(state);
        if (subscription != null) {
            this.subscription = subscription;
            parent.updateSubscription(this.subscription);
            responder.resourceUpdated(this);
        } else {
            responder.invalidRequest("A resource-path is required when updating a UPSSubscription");
        }
    }

    @Override
    public void delete(RequestContext ctx, Responder responder) throws Exception {
        parent.deleteSubscription(subscription.id());
        responder.resourceDeleted( this );
    }
}
