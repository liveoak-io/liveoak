/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.mongo.gridfs;

import java.util.UUID;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mongodb.DB;
import com.mongodb.Mongo;
import io.liveoak.mongo.MongoServices;
import io.liveoak.mongo.gridfs.extension.GridFSExtension;
import io.liveoak.testtools.AbstractHTTPResourceTestCase;
import org.junit.Before;
import org.vertx.java.core.json.JsonObject;

import static org.fest.assertions.Assertions.assertThat;

/**
 * @author <a href="mailto:marko.strukelj@gmail.com">Marko Strukelj</a>
 */
public class AbstractGridFSTest extends AbstractHTTPResourceTestCase {

    protected static final String ALL = "*/*";
    protected static final String APPLICATION_JSON = "application/json";

    protected String BASEPATH = "gridfs";

    protected DB db;
    private Mongo mongoClient;

    @Override
    public void loadExtensions() throws Exception {
        loadExtension("gridfs", new GridFSExtension(), createConfig());
    }

    @Before
    public void getAholdOfMongoThings() throws InterruptedException {
        this.db = (DB) this.system.service(MongoServices.db("testOrg", "testApp", "gridfs"));
        this.mongoClient = (Mongo) this.system.service(MongoServices.mongo("gridfs"));
    }

    public ObjectNode createConfig() {
        String database = System.getProperty("mongo.db", "MongoControllerTest_" + UUID.randomUUID());
        Integer port = new Integer(System.getProperty("mongo.port", "27017"));
        String host = System.getProperty("mongo.host", "localhost");
        System.setProperty("mongo.db", database);
        System.setProperty("mongo.host", host);
        System.setProperty("mongo.port", "" + port);

        ObjectNode config = JsonNodeFactory.instance.objectNode();
        config.put("db", database);
        config.put("port", port);
        config.put("host", host);

        return config;
    }

    protected void assertLink(Object item, String rel, String href) {
        assertThat(item).isInstanceOf(JsonObject.class);
        JsonObject obj = (JsonObject) item;
        assertThat(obj.getString("rel")).isEqualTo(rel);
        assertThat(obj.getString("href")).isEqualTo(href);
    }

}
