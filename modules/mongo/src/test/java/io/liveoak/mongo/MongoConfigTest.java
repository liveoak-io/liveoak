/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.mongo;

import static org.fest.assertions.Assertions.assertThat;
import static org.junit.Assert.fail;

import java.net.UnknownHostException;
import java.util.concurrent.ExecutionException;

import io.liveoak.spi.ResourceException;
import org.junit.Test;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;

import io.liveoak.spi.InitializationException;
import io.liveoak.spi.NotAcceptableException;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.ResourceNotFoundException;
import io.liveoak.spi.state.ResourceState;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 */
public class MongoConfigTest extends BaseMongoDBTest {

    @Test
    public void testReadConfigOnRoot() throws Exception {
        String methodName = "testReadConfigOnRoot";
        assertThat(db.collectionExists(methodName)).isFalse();

        ResourceState result = client.read(new RequestContext.Builder().build(), BASEPATH + ";config");
        assertThat(result.getProperty("host")).isEqualTo(System.getProperty("mongo.host"));
        assertThat(result.getProperty("port")).isEqualTo(Integer.parseInt(System.getProperty("mongo.port")));
        assertThat(result.getProperty("db")).isEqualTo(System.getProperty("mongo.db"));
        assertThat(result.getPropertyNames().size()).isEqualTo(3);
    }

    @Test
    public void testReadConfigOnNonExistantRoot() throws Exception {
        String methodName = "testReadConfigOnNonExistantRoot";
        assertThat(db.collectionExists(methodName)).isFalse();

        try {
            client.read(new RequestContext.Builder().build(), "foo;config");
            fail();
        } catch (ResourceNotFoundException e) {
            // expected
        }
    }

    @Test
    public void testReadConfigOnCollection() throws Exception {
        String methodName = "testReadConfigOnCollection";
        assertThat(db.collectionExists(methodName)).isFalse();
        db.createCollection(methodName, new BasicDBObject());

        // currently we don't have config options on collections, so this should fail
        try {
            client.read(new RequestContext.Builder().build(), BASEPATH + "/" + methodName + ";config");
            fail();
        } catch (NotAcceptableException e) {
            // expected
        }
    }

    @Test
    public void testReadConfigOnNonExistantCollection() throws Exception {
        String methodName = "testReadConfigOnNonExistantCollection";
        assertThat(db.collectionExists(methodName)).isFalse();
        db.createCollection(methodName, new BasicDBObject());

        // currently we don't have config options on collections, so this should fail
        try {
            client.read(new RequestContext.Builder().build(), BASEPATH + "/foo;config");
            fail();
        } catch (ResourceNotFoundException e) {
            // expected
        }
    }

    @Test
    public void testModifyMongoConfig() throws Exception {
        String methodName = "testModifyMongoConfig";
        assertThat(db.collectionExists(methodName)).isFalse();

        DBCollection collectionA = db.createCollection(methodName, new BasicDBObject());
        DBObject dbObjectA = new BasicDBObject("_id", "message");
        dbObjectA.put("text", "hello world");
        collectionA.insert(dbObjectA);

        DB dbB = mongoClient.getDB("testB");
        DBCollection collectionB = dbB.createCollection(methodName, new BasicDBObject());
        DBObject dbObjectB = new BasicDBObject("_id", "message");
        dbObjectB.put("text", "goodbye");
        collectionB.insert(dbObjectB);

        ResourceState configResult = client.read(new RequestContext.Builder().build(), BASEPATH + ";config");
        assertThat(configResult.getProperty("host")).isEqualTo(System.getProperty("mongo.host"));
        assertThat(configResult.getProperty("port")).isEqualTo(Integer.parseInt(System.getProperty("mongo.port")));
        assertThat(configResult.getProperty("db")).isEqualTo(System.getProperty("mongo.db"));
        assertThat(configResult.getPropertyNames().size()).isEqualTo(3);

        ResourceState messageResult = client.read(new RequestContext.Builder().build(), BASEPATH + "/" + methodName + "/message");
        assertThat(messageResult.getProperty("text")).isEqualTo("hello world");

        // update the configuration values
        configResult.putProperty("db", "testB");
        ResourceState updateResult = client.update(new RequestContext.Builder().build(), BASEPATH + ";config", configResult);
        assertThat(updateResult.getProperty("db")).isEqualTo("testB");

        // verify the new configuration values
        ResourceState configResultUpdated = client.read(new RequestContext.Builder().build(), BASEPATH + ";config");
        assertThat(configResultUpdated.getProperty("host")).isEqualTo(System.getProperty("mongo.host"));
        assertThat(configResultUpdated.getProperty("port")).isEqualTo(Integer.parseInt(System.getProperty("mongo.port")));
        assertThat(configResultUpdated.getProperty("db")).isEqualTo("testB");
        assertThat(configResult.getPropertyNames().size()).isEqualTo(3);

        // verify the new result which comes back. note that the url is the same here as the previous messageResult.
        // This could be considered to be similar to switching between a devel and production database
        ResourceState messageResultB = client.read(new RequestContext.Builder().build(), BASEPATH + "/" + methodName + "/message");
        assertThat(messageResultB.getProperty("text")).isEqualTo("goodbye");
    }

    @Test
    public void testModifyMongoInvalidDatabase() throws Exception {
        String methodName = "testModifyMongoInvalidDatabase";
        assertThat(db.collectionExists(methodName)).isFalse();

        ResourceState configResult = client.read(new RequestContext.Builder().build(), BASEPATH + ";config");
        assertThat(configResult.getProperty("host")).isEqualTo(System.getProperty("mongo.host"));
        assertThat(configResult.getProperty("port")).isEqualTo(Integer.parseInt(System.getProperty("mongo.port")));
        assertThat(configResult.getProperty("db")).isEqualTo(System.getProperty("mongo.db"));
        assertThat(configResult.getPropertyNames().size()).isEqualTo(3);

        // update the configuration values
        configResult.putProperty("db", "");

        try {
            client.update(new RequestContext.Builder().build(), BASEPATH + ";config", configResult);
            fail();
        } catch (ResourceException e) {
            //expected
        }

        configResult.putProperty("db", null);

        try {
            client.update(new RequestContext.Builder().build(), BASEPATH + ";config", configResult);
            fail();
        } catch (ResourceException e) {
           //expected
        }
    }

    @Test
    public void testModifyMongoInvalidPort() throws Exception {
        String methodName = "testModifyMongoInvalidPort";
        assertThat(db.collectionExists(methodName)).isFalse();

        ResourceState configResult = client.read(new RequestContext.Builder().build(), BASEPATH + ";config");
        assertThat(configResult.getProperty("host")).isEqualTo(System.getProperty("mongo.host"));
        assertThat(configResult.getProperty("port")).isEqualTo(Integer.parseInt(System.getProperty("mongo.port")));
        assertThat(configResult.getProperty("db")).isEqualTo(System.getProperty("mongo.db"));
        assertThat(configResult.getPropertyNames().size()).isEqualTo(3);

        // update the configuration values
        configResult.putProperty("port", "");

        try {
            client.update(new RequestContext.Builder().build(), BASEPATH + ";config", configResult);
            fail();
        } catch (ResourceException e) {
            //expected
        }

        configResult.putProperty("port", null);

        try {
            client.update(new RequestContext.Builder().build(), BASEPATH + ";config", configResult);
            fail();
        } catch (ResourceException e) {
            //expected
        }

        configResult.putProperty("port", "helloworld");

        try {
            client.update(new RequestContext.Builder().build(), BASEPATH + ";config", configResult);
            fail();
        } catch (ResourceException e) {
            //expected
        }

        configResult.putProperty("port", "1234"); // Note: its a string, not an integer

        try {
            client.update(new RequestContext.Builder().build(), BASEPATH + ";config", configResult);
            fail();
        } catch (ResourceException e) {
            //expected
        }

        // setting to an int should not fail
        configResult.putProperty("port", 1234);
        ResourceState state = client.update(new RequestContext.Builder().build(), BASEPATH + ";config", configResult);
        assertThat(state.getProperty("port")).isEqualTo(1234);

    }

    @Test
    public void testModifyMongoInvalidHost() throws Exception {
        String methodName = "testModifyMongoInvalidHost";
        assertThat(db.collectionExists(methodName)).isFalse();

        ResourceState configResult = client.read(new RequestContext.Builder().build(), BASEPATH + ";config");
        assertThat(configResult.getProperty("host")).isEqualTo(System.getProperty("mongo.host"));
        assertThat(configResult.getProperty("port")).isEqualTo(Integer.parseInt(System.getProperty("mongo.port")));
        assertThat(configResult.getProperty("db")).isEqualTo(System.getProperty("mongo.db"));
        assertThat(configResult.getPropertyNames().size()).isEqualTo(3);

        // update the configuration values
        configResult.putProperty("host", "");

        try {
            client.update(new RequestContext.Builder().build(), BASEPATH + ";config", configResult);
            fail();
        } catch (ResourceException e) {
            //expected
        }

        configResult.putProperty("host", null);

        try {
            client.update(new RequestContext.Builder().build(), BASEPATH + ";config", configResult);
            fail();
        } catch (ResourceException e) {
            //expected
        }

        configResult.putProperty("host", "helloworld");

        try {
            client.update(new RequestContext.Builder().build(), BASEPATH + ";config", configResult);
            fail();
        } catch (ResourceException e) {
            //expected
        }

        // setting to an int should fail
        configResult.putProperty("host", 1234);
        try {
            client.update(new RequestContext.Builder().build(), BASEPATH + ";config", configResult);
            fail();
        } catch (ResourceException e) {
            //expected
        }

    }

    // TODO: test actually connecting on another the host and port, but this becomes tricky as it requires another mongo server to be started

}
