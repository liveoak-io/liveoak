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
import io.liveoak.ups.resource.config.UPSRootConfigResource;
import org.jboss.aerogear.unifiedpush.JavaSender;
import org.jboss.aerogear.unifiedpush.SenderClient;
import org.jboss.aerogear.unifiedpush.message.MessageResponseCallback;
import org.jboss.aerogear.unifiedpush.message.UnifiedMessage;
import org.jboss.logging.Logger;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 */
public class UPSSubscription implements Subscription, Resource {

    private static final Logger log = Logger.getLogger("io.liveoak.ups");

    // LiveOAK specifics
    static final String RESOURCE_PATH = "resourcePath";

    // UPS specifics
    // try and match the names as closely as possible as to what is normally sent to UPS
    static final String VARIANTS = "variants";
    static final String ALIASES = "alias";
    static final String CATEGORIES = "categories";
    static final String DEVICES = "deviceType";
    static final String SIMPLE_PUSH = "simple-push";
    static final String MESSAGE = "message";

    //LiveOak specifics to be added as attribute to the message
    static final String LIVEOAK_RESOURCE_URL = "io.liveoak.push.url";
    static final String LIVEOAK_RESOURCE_EVENT = "io.liveoak.push.event";

    ResourcePath resourcePath;
    String id;
    UPSSubscriptionsResource parent;
    UPSRootConfigResource config;

    Set<String> categories = new HashSet<>();
    String simplePush;
    List<String> deviceTypes = new ArrayList<String>();
    List<String> variants = new ArrayList<String>();
    List<String> aliases = new ArrayList<String>();

    UPSMessageResource upsMessageResource;


    private UPSSubscription(UPSSubscriptionsResource parent, UPSRootConfigResource config, String id, String path) {
        this.parent = parent;
        this.config = config;
        this.id = id;
        this.resourcePath = new ResourcePath(path);
        this.upsMessageResource = new UPSMessageResource(this);
    }

    public static UPSSubscription create(UPSSubscriptionsResource parent, UPSRootConfigResource config, RequestContext ctx, ResourceState state, Responder responder) {
        String path = (String)state.getProperty(RESOURCE_PATH);

        String id = state.id();
        if (id == null) {
            id = UUID.randomUUID().toString();
        }

        UPSSubscription subscription = new UPSSubscription(parent, config, id, path);

        List<String> variants = (List) state.getProperty( VARIANTS );
        if (variants != null) {
            subscription.variants = variants;
        }

        List<String> aliases = (List) state.getProperty( ALIASES );
        if (aliases != null) {
            subscription.aliases = aliases;
        }

        List<String> categories = (List) state.getProperty( CATEGORIES );
        if (categories != null) {
            subscription.categories = new HashSet(categories);
        }

        List<String> deviceTypes = (List) state.getProperty(DEVICES);
        if (deviceTypes != null) {
            subscription.deviceTypes = deviceTypes;
        }

        String simplePush = (String) state.getProperty( SIMPLE_PUSH );
        if (simplePush != null) {
            subscription.simplePush = simplePush;
        }

        ResourceState message = (ResourceState) state.getProperty(MESSAGE);
        if (message != null) {
            Map<String, Object> attributes = new HashMap();
            for (String property : message.getPropertyNames()) {
                attributes.put( property, message.getProperty(property));
            }
            subscription.upsMessageResource.setAttributes( attributes );
        }

        System.currentTimeMillis();

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
        return this.resourcePath;
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
        throw new IllegalStateException("Authorization error not expected for UPSSubscription");
    }

    /** Subscription Methods **/

    @Override
    public void resourceCreated( ResourceResponse resourceResponse ) throws Exception {
        this.sendMessageToUPS(resourceResponse.resource().uri(), "created");
    }

    @Override
    public void resourceUpdated( ResourceResponse resourceResponse ) throws Exception {
        this.sendMessageToUPS(resourceResponse.resource().uri(), "updated");
    }

    @Override
    public void resourceDeleted( ResourceResponse resourceResponse ) throws Exception {
        this.sendMessageToUPS(resourceResponse.resource().uri(), "deleted");
    }

    private void sendMessageToUPS(URI uri, String eventType) throws Exception {
        JavaSender sender = new SenderClient(config.getUPSServerURL());

        // setup the application specifics
        UnifiedMessage.Builder builder = new UnifiedMessage.Builder()
                .pushApplicationId(config.getApplicationId())
                .masterSecret(config.getMasterSecret());

        // setup who is to receive the message
        if (variants != null) {
            builder.variants( variants );
        }

        if (aliases != null) {
            builder.aliases( aliases );
        }

        if (categories != null) {
            builder.categories( categories );
        }

        if (deviceTypes != null) {
            builder.deviceType( deviceTypes );
        }

        if (simplePush != null) {
            builder.simplePush( simplePush );
        }

        //setup the message itself
        if (upsMessageResource != null && upsMessageResource.getAttributes() != null) {
            //NOTE: this clears any previously set attributes, any attributes which need to be set have to come after this
            builder.attributes( new HashMap(upsMessageResource.getAttributes()) ) ;
        }

        // LiveOak specifics used by client to determine which resource and event type occurred
        builder.attribute( LIVEOAK_RESOURCE_URL, uri.toString());
        builder.attribute( LIVEOAK_RESOURCE_EVENT, eventType);

        sender.send( builder.build(), new MessageResponseCallback() {
            @Override
            public void onComplete( int i ) {
                //do nothing for now
            }

            @Override
            public void onError( Throwable throwable ) {
               //if we received an error, log the error.
               //TODO: should we throw the error here?
               log.error( "Error trying to send notification to UPS server", throwable );
            }
        } );

    }

    /** Resource Methods  **/

    @Override
    public void readProperties(RequestContext ctx, PropertySink sink) throws Exception {
        sink.accept(this.RESOURCE_PATH, this.resourcePath.toString());
        sink.accept(this.VARIANTS, this.variants);
        sink.accept(this.ALIASES ,this.aliases);
        sink.accept(this.CATEGORIES, this.categories);
        sink.accept(this.DEVICES, this.deviceTypes);
        sink.accept(this.SIMPLE_PUSH, this.simplePush);
        sink.accept(UPSMessageResource.ID, this.upsMessageResource);
        sink.close();
    }

    @Override
    public void updateProperties(RequestContext ctx, ResourceState state, Responder responder) throws Exception {
        String path = (String)state.getProperty(RESOURCE_PATH);

        if (path != null) {
            this.resourcePath = new ResourcePath(path);
        }

        List<String> variants = (List) state.getProperty( VARIANTS );
        if (variants != null) {
            this.variants = variants;
        }

        List<String> aliases = (List) state.getProperty( ALIASES );
        if (aliases != null) {
            this.aliases = aliases;
        }

        List<String> categories = (List) state.getProperty( CATEGORIES );
        if (categories != null) {
            this.categories = new HashSet(categories);
        }

        List<String> deviceTypes = (List) state.getProperty(DEVICES);
        if (deviceTypes != null) {
            this.deviceTypes = deviceTypes;
        }

        String simplePush = (String) state.getProperty( SIMPLE_PUSH );
        if (simplePush != null) {
            this.simplePush = simplePush;
        }

        ResourceState message = (ResourceState) state.getProperty(MESSAGE);
        if (message != null) {
            Map<String, Object> attributes = new HashMap();
            for (String property : message.getPropertyNames()) {
                attributes.put( property, message.getProperty(property));
            }
            this.upsMessageResource.setAttributes( attributes );
        }

        responder.resourceUpdated( this );
    }

    @Override
    public void readMembers(RequestContext ctx, ResourceSink sink) throws Exception {
        sink.close();
    }

    @Override
    public void readMember(RequestContext ctx, String id, Responder responder) throws Exception {
        responder.noSuchResource( id );
    }

}
