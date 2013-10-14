package org.projectodd.restafari.container.eventbus;

import org.projectodd.restafari.container.Container;
import org.projectodd.restafari.container.Holder;
import org.projectodd.restafari.container.ResourcePath;
import org.projectodd.restafari.spi.Config;
import org.projectodd.restafari.spi.InitializationException;
import org.projectodd.restafari.spi.ResourceController;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonObject;

public class EventBusBridgeContainer extends Container {

    @Override
    public void registerResourceController(String type, ResourceController controller, Config config) throws InitializationException {
        super.registerResourceController(type, controller, config);

        // TODO: This could really use something like Netty's ChannelPipeline
        EventBus eventBus = getVertx().eventBus();
        eventBus.registerHandler(type + ".get", (Message<String> message) -> {
            System.err.println("!!! Got Message " + message);
            ResourcePath path = new ResourcePath("/" + type + "/" + message.body());
            String mimeType = "application/json"; //TODO: Gonna have to figure out how/if to specify this
            System.err.println("!!! Path is " + path.getFullPath());
            if (path.isCollectionPath() ) {
                System.err.println("!!! isCollectionPath with type " + path.getType() + " and name " + path.getCollectionName());
                Holder holder = this.getResourceController(type);
                holder.getResourceController().getResources(null, path.getCollectionName(), null, new EventBusResponderImpl(mimeType, message, this.getCodecManager()));
            } else if (path.isResourcePath()) {
                Holder holder = this.getResourceController(type);
                holder.getResourceController().getResource(null, path.getCollectionName(), path.getResourceId(), new EventBusResponderImpl(mimeType, message, this.getCodecManager()));
            }
        }, asyncResult -> {
            System.err.println("!!! Registered eventbus handler");
        });
    }
}
