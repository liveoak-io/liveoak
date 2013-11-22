/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.vertx.modules.server;

import io.liveoak.container.UnsecureServer;
import org.vertx.java.core.Future;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.platform.Verticle;

import java.net.UnknownHostException;

/*
This is a simple Java verticle which starts the server
 */
public class ServerVerticle extends Verticle {

    private UnsecureServer server;
    private ResourceDeployer deployer;

    @Override
    public void start(Future<Void> startResult) {
        JsonObject config = this.container.config();

        String host = config.getString("host", "localhost");
        int port = config.getInteger("port", 8080);

        try {
            server = new UnsecureServer(this.vertx, host, port);
        } catch (UnknownHostException e) {
            startResult.setFailure(e);
            return;
        }

        try {
            server.start();
        } catch (InterruptedException e) {
            startResult.setFailure(e);
        }

        String address = config.getString("address", "server.resource.registration");

        this.deployer = new ResourceDeployer(this.server.container(), address);

        startResult.setResult(null);
    }

    @Override
    public void stop() {
        if (server == null)
            return;
        try {
            server.stop();
        } catch (InterruptedException ignored) {
        }
    }
}
