/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.mongo.gridfs;

import java.util.UUID;

import com.mongodb.DB;
import com.mongodb.MongoClient;
import io.liveoak.common.codec.DefaultResourceState;
import io.liveoak.spi.resource.RootResource;
import io.liveoak.spi.state.ResourceState;
import io.liveoak.testtools.AbstractHTTPResourceTestCase;
import org.junit.After;
import org.vertx.java.core.json.JsonObject;

import static org.fest.assertions.Assertions.assertThat;

/**
 * @author <a href="mailto:marko.strukelj@gmail.com">Marko Strukelj</a>
 */
public class AbstractGridFSTest extends AbstractHTTPResourceTestCase {

    protected static final String ALL = "*/*";
    protected static final String APPLICATION_JSON = "application/json";

    protected String BASEPATH = "gridfs";

    private DB db;

    @Override
    public RootResource createRootResource() {
        return new GridFSRootResource(BASEPATH);
    }

    @Override
    public ResourceState createConfig() {
        String database = System.getProperty("mongo.db", "MongoGridFSTest_" + UUID.randomUUID());
        Integer port = new Integer(System.getProperty("mongo.port", "27019"));
        String host = System.getProperty("mongo.host", "localhost");

        ResourceState config = new DefaultResourceState();
        config.putProperty("db", database);
        config.putProperty("port", port);
        config.putProperty("host", host);

        try {
            MongoClient mongoClient = new MongoClient(host, port);
            db = mongoClient.getDB(database);
            db.dropDatabase();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return config;
    }

    @After
    public void tearDownDB() throws Exception {
        db.dropDatabase();
    }

    protected void assertLink(Object item, String rel, String href) {
        assertThat(item).isInstanceOf(JsonObject.class);
        JsonObject obj = (JsonObject) item;
        assertThat(obj.getString("rel")).isEqualTo(rel);
        assertThat(obj.getString("href")).isEqualTo(href);
    }

}
