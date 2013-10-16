package org.projectodd.restafari.container;

import java.util.HashMap;
import java.util.Map;

import org.projectodd.restafari.container.codec.ResourceCodec;
import org.projectodd.restafari.container.codec.ResourceCodecManager;
import org.projectodd.restafari.container.codec.json.JSONDecoder;
import org.projectodd.restafari.container.codec.json.JSONEncoder;
import org.projectodd.restafari.container.subscriptions.SubscriptionManager;
import org.projectodd.restafari.spi.*;
import org.projectodd.restafari.spi.state.ResourceState;
import org.vertx.java.core.Vertx;
import org.vertx.java.platform.PlatformLocator;
import org.vertx.java.platform.PlatformManager;


public class DefaultContainer implements Container, CollectionResource {

    public DefaultContainer() {
        this.codecManager.registerResourceCodec("application/json", new ResourceCodec( new JSONEncoder(), new JSONDecoder() ) );
        PlatformManager platformManager = PlatformLocator.factory.createPlatformManager();
        this.vertx = platformManager.vertx();

        this.subscriptionManager = new SubscriptionManager();
    }

    public void registerResource(RootResource resource, Config config) throws InitializationException {
        //TODO: Lazy initialization in holder class when resourceRead controller is first accessed
        resource.initialize(new SimpleResourceContext(this.vertx, null, config));
        this.resources.put(resource.id(), resource);
    }

    public ResourceCodecManager getCodecManager() {
        return this.codecManager;
    }

    public Vertx vertx() {
        return this.vertx;
    }

    SubscriptionManager getSubscriptionManager() {
        return this.subscriptionManager;
    }

    // ----------------------------------------
    // CollectionResource
    // ----------------------------------------

    @Override
    public void read(String id, Responder responder) {
        if (!this.resources.containsKey(id)) {
            System.err.println( resources );
            responder.noSuchResource(id);
            return;
        }

        responder.resourceRead(this.resources.get(id));
    }

    @Override
    public void delete(Responder responder) {
        responder.deleteNotSupported(this);
    }

    @Override
    public void read(Pagination pagination, Responder responder) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void create(ResourceState state, Responder responder) {
        responder.createNotSupported(this);
    }

    @Override
    public void writeMembers(ResourceSink sink) {
        this.resources.values().forEach((e) -> {
            sink.accept( e );
        });
        sink.close();
    }

    @Override
    public Resource parent() {
        return null;
    }

    @Override
    public String id() {
        return this.prefix;
    }

    private String prefix = "";
    private Map<String, Resource> resources = new HashMap<>();
    private ResourceCodecManager codecManager = new ResourceCodecManager();
    private Vertx vertx;
    private final SubscriptionManager subscriptionManager;

}


