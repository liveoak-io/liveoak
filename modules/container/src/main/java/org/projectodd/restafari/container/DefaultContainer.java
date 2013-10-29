package org.projectodd.restafari.container;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.projectodd.restafari.container.codec.ResourceCodec;
import org.projectodd.restafari.container.codec.ResourceCodecManager;
import org.projectodd.restafari.container.codec.aggregating.AggregatingEncoder;
import org.projectodd.restafari.container.codec.html.HTMLEncoder;
import org.projectodd.restafari.container.codec.json.JSONDecoder;
import org.projectodd.restafari.container.codec.json.JSONEncoder;
import org.projectodd.restafari.container.subscriptions.SubscriptionManager;
import org.projectodd.restafari.spi.*;
import org.projectodd.restafari.spi.resource.Resource;
import org.projectodd.restafari.spi.resource.RootResource;
import org.projectodd.restafari.spi.resource.async.CollectionResource;
import org.projectodd.restafari.spi.resource.async.ResourceSink;
import org.projectodd.restafari.spi.resource.async.Responder;
import org.projectodd.restafari.spi.state.ResourceState;
import org.vertx.java.core.Vertx;
import org.vertx.java.platform.PlatformLocator;
import org.vertx.java.platform.PlatformManager;


public class DefaultContainer implements Container, CollectionResource {

    public DefaultContainer() {
        this.codecManager.registerResourceCodec("application/json", new ResourceCodec( new JSONEncoder(), new JSONDecoder() ) );
        this.codecManager.registerResourceCodec("text/html", new ResourceCodec( new HTMLEncoder(), null ) );
        //this.codecManager.registerResourceCodec("application/javascript" , new ResourceCodec( new AggregatingEncoder( MediaType.JAVASCRIPT), null ) );

        PlatformManager platformManager = PlatformLocator.factory.createPlatformManager();
        this.vertx = platformManager.vertx();

        this.subscriptionManager = new SubscriptionManager();
        this.workerPool = Executors.newCachedThreadPool();
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

    Executor workerPool() {
        return this.workerPool;
    }

    public DirectConnector directConnector() {
        return new DirectConnector( this );
    }

    // ----------------------------------------
    // CollectionResource
    // ----------------------------------------

    @Override
    public void read(String id, Responder responder) {
        try {
            if ( id == null ) {
                responder.resourceRead( this );
                return;
            }

            if (!this.resources.containsKey(id)) {
                responder.noSuchResource(id);
                return;
            }

            responder.resourceRead(this.resources.get(id));

        } catch (Throwable t) {
            responder.internalError(t.getMessage());
        }
    }

    @Override
    public void delete(Responder responder) {
        responder.deleteNotSupported(this);
    }

    @Override
    public void create(ResourceState state, Responder responder) {
        responder.createNotSupported(this);
    }

    @Override
    public void readContent(Pagination pagination, ResourceSink sink) {
        this.resources.values().forEach((e) -> {
            sink.accept( e );
        });
        try {
            sink.close();
        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
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
    private Executor workerPool;

}


