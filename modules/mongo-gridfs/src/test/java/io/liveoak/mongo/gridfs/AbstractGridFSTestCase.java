/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.mongo.gridfs;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.mongodb.DB;
import com.mongodb.Mongo;
import com.mongodb.MongoClient;
import io.liveoak.common.codec.DefaultResourceState;
import io.liveoak.mongo.gridfs.extension.GridFSExtension;
import io.liveoak.spi.LiveOak;
import io.liveoak.spi.state.ResourceState;
import io.liveoak.testtools.AbstractHTTPResourceTestCaseWithTestApp;
import org.jboss.logging.Logger;
import org.junit.BeforeClass;
import org.vertx.java.core.json.JsonObject;

import static org.fest.assertions.Assertions.assertThat;

/**
 * @author <a href="mailto:marko.strukelj@gmail.com">Marko Strukelj</a>
 */
public class AbstractGridFSTestCase extends AbstractHTTPResourceTestCaseWithTestApp {

    protected static final String ALL = "*/*";
    protected static final String APPLICATION_JSON = "application/json";

    protected static final Logger log = Logger.getLogger(AbstractGridFSTestCase.class);

    protected String BASEPATH = "gridfs";

    protected static DB db;
    private static Mongo mongoClient;

    @BeforeClass
    public static void loadExtensions() throws Exception {
        loadExtension("gridfs", new GridFSExtension());
        installTestAppResource("gridfs", "gridfs", createConfig());
    }

    public static ResourceState createConfig() {
        String database = System.getProperty("mongo.db", "MongoControllerTest_" + UUID.randomUUID());
        Integer port = new Integer(System.getProperty("mongo.port", "27017"));
        String host = System.getProperty("mongo.host", "localhost");
        log.debug("Using Mongo on " + host + ":" + port + ", database: " + database);

        ResourceState config = new DefaultResourceState();
        config.putProperty("db", database);

        List<ResourceState> servers = new ArrayList<ResourceState>();
        ResourceState server = new DefaultResourceState();
        server.putProperty("port", port);
        server.putProperty("host", host);
        servers.add(server);
        config.putProperty("servers", servers);

        try {
            mongoClient = new MongoClient(host, port);
            db = mongoClient.getDB(database);
            db.dropDatabase();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return config;
    }

    protected void assertLink(Object item, String rel, String href) {
        assertThat(item).isInstanceOf(JsonObject.class);
        JsonObject obj = (JsonObject) item;
        assertThat(obj.getString("rel")).isEqualTo(rel);
        assertThat(obj.getString(LiveOak.HREF)).isEqualTo(href);
    }

}
