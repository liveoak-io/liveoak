/*
 * Copyright 2013 Red Hat, Inc.
 *
 * Red Hat licenses this file to you under the Apache License, version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License.  You may obtain a copy of the License at:
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
 *
 * @author <a href="marko.strukelj@gmail.com">Marko Strukelj</a>
 */
package org.projectodd.restafari.vertx.modules.server;

import org.projectodd.restafari.container.UnsecureServer;
import org.vertx.java.core.Future;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.platform.Verticle;

import java.net.UnknownHostException;

/*
This is a simple Java verticle which starts the server
 */
public class ServerVerticle extends Verticle {

    private UnsecureServer server;

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
