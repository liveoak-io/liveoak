package org.projectodd.restafari.vertx.modules.server;

import org.projectodd.restafari.container.DefaultContainer;
import org.projectodd.restafari.container.SimpleConfig;
import org.projectodd.restafari.spi.InitializationException;
import org.projectodd.restafari.vertx.resource.RootVertxCollectionResource;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonObject;

/**
 * @author Bob McWhirter
 */
public class ResourceDeployer {

    public ResourceDeployer(DefaultContainer container, String address) {
        this.container = container;

        this.container.vertx().eventBus().registerHandler(address, (Message<JsonObject> message) -> {
            String action = message.body().getString("action");
            if (action.equals("register")) {
                String id = message.body().getString("id");
                String resourceAddress = message.body().getString("address");
                try {
                    this.container.registerResource(new RootVertxCollectionResource(id, resourceAddress), new SimpleConfig());
                } catch (InitializationException e) {
                    e.printStackTrace();
                }
            } else if (action.equals("unregister")) {

            } else {

            }

        });
    }

    private DefaultContainer container;
}
