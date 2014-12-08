/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.mongo;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.Mongo;
import com.mongodb.MongoClient;
import com.mongodb.WriteConcern;
import com.mongodb.util.JSON;
import io.liveoak.common.codec.DefaultResourceState;
import io.liveoak.mongo.extension.MongoExtension;
import io.liveoak.spi.state.ResourceState;
import io.liveoak.testtools.AbstractTestCaseWithTestApp;
import org.jboss.logging.Logger;
import org.junit.BeforeClass;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 */
public class BaseMongoDBTest extends AbstractTestCaseWithTestApp {

    protected static String BASEPATH = "storage";

    protected static DB db;
    protected static Mongo mongoClient;

    protected static final Logger log = Logger.getLogger(BaseMongoDBTest.class);

    @BeforeClass
    public static void loadExtensions() throws Exception {
        ObjectNode config = JsonNodeFactory.instance.objectNode();
        config.put("db", "MongoInteralTest_" + UUID.randomUUID());
        config.put("name", "Default Datastore");

        loadExtension("mongo", new MongoExtension(), config);
        installTestAppResource("mongo", BASEPATH, createConfig());
    }

    public static ResourceState createConfig() {
        String database = System.getProperty("mongo.db", "MongoControllerTest_" + UUID.randomUUID());
        Integer port = new Integer(System.getProperty("mongo.port", "27017"));
        String host = System.getProperty("mongo.host", "localhost");
        log.debug("Using Mongo on " + host + ":" + port + ", database: " + database);
        System.setProperty("mongo.db", database);
        System.setProperty("mongo.host", host);
        System.setProperty("mongo.port", "" + port);

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
            db.setWriteConcern(WriteConcern.ACKNOWLEDGED);
            db.dropDatabase();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return config;
    }

    protected void setupPeopleData(DBCollection collection) {
        // add a few people
        String[] data = {
                "{name:'John', lastName:'Doe', country:'US', city:'San Francisco', identity:{type:'Facebook', id:'84904785333'}}",
                "{name:'Jane', lastName:'Doe', country:'US', city:'New York', identity:{type:'Facebook', id:'89734343300'}}",
                "{name:'Hans', lastName:'Gruber', country:'DE', city:'Berlin', identity:{type:'Google', id:'63aaa9090f0'}}",
                "{name:'Helga', lastName:'Schmidt', country:'DE', city:'Munich', identity:{type:'Google', id:'8eb490ff90'}}",
                "{name:'Francois', lastName:'Popo', country:'FR', city:'Marseille', identity:{type:'Google', id:'a3b2b16429'}}",
                "{name:'Jacqueline', lastName:'Coco', country:'FR', city:'Paris', identity:{type:'Facebook', id:'328874222000'}}",
        };

        addPeopleItems(collection, data);
    }

    protected void addPeopleItems(DBCollection collection, String[] data) {
        for (String rec : data) {
            BasicDBObject obj = (BasicDBObject) JSON.parse(rec);
            collection.insert(obj);
        }
    }

}
