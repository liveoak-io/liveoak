/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.mongo.gridfs;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mongodb.DB;
import com.mongodb.Mongo;
import io.liveoak.mongo.MongoServices;
import io.liveoak.mongo.gridfs.extension.GridFSExtension;
import io.liveoak.testtools.AbstractResourceTestCase;
import org.junit.Before;

import java.util.UUID;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 */
public class BaseGridFSTest extends AbstractResourceTestCase {

    protected String BASEPATH = "gridfs";

    protected static Mongo mongoClient;
    protected static DB db;

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
}
