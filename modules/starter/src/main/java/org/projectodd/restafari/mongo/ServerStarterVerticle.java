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
package org.projectodd.restafari.mongo;

import io.netty.channel.nio.NioEventLoopGroup;
import org.projectodd.restafari.container.DefaultContainer;
import org.projectodd.restafari.container.SimpleConfig;
import org.projectodd.restafari.container.UnsecureServer;
import org.projectodd.restafari.spi.InitializationException;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.platform.Verticle;

import java.net.InetAddress;
import java.net.UnknownHostException;

/*
This is a simple Java verticle which starts the server
 */
public class ServerStarterVerticle extends Verticle {

    private UnsecureServer server;

    @Override
    public void start() {

        DefaultContainer container = new DefaultContainer();
        try {
            // mongo db configuration
            JsonObject conf = getConf("storage");
            SimpleConfig config = new SimpleConfig();
            setNonNullOnly(config, "db", getWithFailOver(System.getProperty("mbaas.mongo.db"), conf.getString("db"), "mydb"));
            setNonNullOnly(config, "host", getWithFailOver(System.getProperty("mbaas.mongo.host"), conf.getString("host"), "localhost"));
            String mongoPortProperty = System.getProperty("mbaas.mongo.port");
            setNonNullOnly(config, "port", getWithFailOver(mongoPortProperty == null ? null : Integer.parseInt(mongoPortProperty), conf.getInteger("port")));

            container.registerResource(new MongoDBResource("storage"), config);
        } catch (InitializationException e) {
            throw new RuntimeException("Failed to initialize object controller: MongoController", e);
        }

        /*
        try {
            // deployer configuration
            JsonObject conf = getConf("deployer");
            SimpleConfig config = new SimpleConfig();
            setNonNullOnly(config, "mbaas.deployer.dir", getWithFailOver(
                    System.getProperty("mbaas.deployer.dir"),
                    conf.getString("dir"),
                    System.getProperty("user.dir")));

            container.registerResourceController("deployer", new DeployerController(), config);
        } catch (InitializationException e) {
            throw new RuntimeException("Failed to initialize object controller: DeployerController", e);
        }
        */

        try {
            server = new UnsecureServer(container, InetAddress.getByName("localhost"), 8080, new NioEventLoopGroup());
        } catch (UnknownHostException e) {
            throw new RuntimeException("Failed to bind server to localhost: ", e);
        }

        try {
            server.start();
        } catch (InterruptedException e) {
            throw new RuntimeException("Must exit - interrupted");
        }

        System.err.println("ServerStarterVerticle started");
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

    private JsonObject getConf(String name) {

        org.vertx.java.platform.Container cont = getContainer();
        JsonObject conf = cont != null ? cont.config() : null;

        if (conf != null) {
            conf = conf.getObject(name);
        }
        return conf != null ? conf : new JsonObject();
    }

    private <T> T getWithFailOver(T... val) {
        for (T v: val) {
            if (v != null && String.valueOf(v).length() > 0)
                return v;
        }
        return null;
    }

    private void setNonNullOnly(SimpleConfig conf, String key, Object value) {
        if (value != null)
            conf.put(key, value);
    }
}
