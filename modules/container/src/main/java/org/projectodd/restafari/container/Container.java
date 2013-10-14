package org.projectodd.restafari.container;

import java.util.HashMap;
import java.util.Map;

import org.projectodd.restafari.container.codec.ResourceCodecManager;
import org.projectodd.restafari.container.codec.json.JSONCodec;
import org.projectodd.restafari.container.subscriptions.SubscriptionManager;
import org.projectodd.restafari.spi.Config;
import org.projectodd.restafari.spi.InitializationException;
import org.projectodd.restafari.spi.ResourceController;
import org.vertx.java.core.Vertx;
import org.vertx.java.platform.PlatformLocator;
import org.vertx.java.platform.PlatformManager;


public class Container {

    public Container() {
        this.codecManager.registerResourceCodec( "application/json", new JSONCodec() );
        PlatformManager platformManager = PlatformLocator.factory.createPlatformManager();
        this.vertx = platformManager.vertx();

        this.subscriptionManager = new SubscriptionManager();
    }

    public void registerResourceController(String type, ResourceController controller, Config config) throws InitializationException {
        //TODO: Lazy initialization in holder class when resource controller is first accessed
        controller.initialize(new SimpleControllerContext(this.vertx, null, config));
        this.controllers.put( type, new Holder( controller ) );
    }

    public Holder getResourceController(String type) {
        return this.controllers.get( type );
    }

    public ResourceCodecManager getCodecManager() {
        return this.codecManager;
    }

    public Vertx getVertx() {
        return this.vertx;
    }

    SubscriptionManager getSubscriptionManager() {
        return this.subscriptionManager;
    }

    private Map<String,Holder> controllers = new HashMap<>();
    private ResourceCodecManager codecManager = new ResourceCodecManager();
    private Vertx vertx;
    private final SubscriptionManager subscriptionManager;


}


