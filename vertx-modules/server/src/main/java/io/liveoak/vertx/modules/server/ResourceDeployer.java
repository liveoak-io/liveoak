/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.vertx.modules.server;

import io.liveoak.container.LiveOakSystem;
import io.liveoak.vertx.resource.RootVertxCollectionResource;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonObject;

/**
 * @author Bob McWhirter
 */
public class ResourceDeployer {

    public ResourceDeployer(LiveOakSystem system, String address) {
        system.vertx().eventBus().registerHandler(address, (Message<JsonObject> message) -> {
            String action = message.body().getString("action");
            if (action.equals("register")) {
                String id = message.body().getString("id");
                String resourceAddress = message.body().getString("address");
                //system.container().registerResource(new RootVertxCollectionResource(id, resourceAddress));
            } else if (action.equals("unregister")) {

            } else {

            }

        });
    }
}
