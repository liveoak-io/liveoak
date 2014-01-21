/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */

package io.liveoak.mongo;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.Mongo;
import com.mongodb.util.JSON;
import io.liveoak.mongo.extension.MongoExtension;
import io.liveoak.testtools.AbstractResourceTestCase;
import org.jboss.logging.Logger;
import org.junit.Before;

import java.util.UUID;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 */
public class BaseMongoDBTest extends AbstractResourceTestCase {

    protected String BASEPATH = "storage";

    protected DB db;
    protected Mongo mongoClient;

    protected final Logger log = Logger.getLogger(getClass());

    @Override
    public void loadExtensions() throws Exception {
        loadExtension("storage", new MongoExtension(), createConfig());
    }

    @Before
    public void getAholdOfMongoThings() throws InterruptedException {
        this.db = (DB) this.system.service(MongoServices.db("testOrg", "testApp", "storage"));
        this.mongoClient = (Mongo) this.system.service(MongoServices.mongo("storage"));
    }

    public ObjectNode createConfig() {
        String database = System.getProperty("mongo.db", "MongoControllerTest_" + UUID.randomUUID());
        Integer port = new Integer(System.getProperty("mongo.port", "27017"));
        String host = System.getProperty("mongo.host", "localhost");
        log.debug("Using Mongo on " + host + ":" + port + ", database: " + database);
        System.setProperty("mongo.db", database);
        System.setProperty("mongo.host", host);
        System.setProperty("mongo.port", "" + port);

        ObjectNode config = JsonNodeFactory.instance.objectNode();
        config.put("db", database);
        config.put("port", port);
        config.put("host", host);

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
