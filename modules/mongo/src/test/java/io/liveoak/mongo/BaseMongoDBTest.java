/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.mongo;

import com.mongodb.DB;
import com.mongodb.MongoClient;
import io.liveoak.container.DefaultContainer;
import io.liveoak.container.SimpleConfig;
import io.liveoak.container.UnsecureServer;
import io.netty.handler.codec.http.HttpHeaders;
import org.apache.http.client.methods.*;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.AfterClass;
import org.junit.BeforeClass;

import java.util.UUID;

/**
 * Created with IntelliJ IDEA.
 * User: mwringe
 * Date: 18/10/13
 * Time: 10:29 AM
 * To change this template use File | Settings | File Templates.
 */
public class BaseMongoDBTest {

    protected static UnsecureServer server;
    protected static MongoClient mongoClient;
    protected static DB db;

    protected static String baseURL;
    protected static final String TYPE = "storage";

    @BeforeClass
    public static void init() throws Exception {
        String database = System.getProperty("mongo.db", "MongoResourceTest_" + UUID.randomUUID());
        Integer port = new Integer(System.getProperty("mongo.port", "27017"));
        String host = System.getProperty("mongo.host", "localhost");

        // configure the mongo controller
        SimpleConfig config = new SimpleConfig();
        config.put("db", database);
        config.put("port", port);
        config.put("host", host);

        DefaultContainer container = new DefaultContainer();
        container.registerResource(new RootMongoResource(TYPE), config);

        //TODO: pass these params in instead of hardcoding them
        server = new UnsecureServer(container, "localhost", 8080);
        server.start();

        baseURL = "http://localhost:8080/" + TYPE;

        // configure a local mongo client to verify the data methods
        mongoClient = new MongoClient(host, port);
        db = mongoClient.getDB(database);
        db.dropDatabase();
    }

    @AfterClass
    public static void dispose() {
        mongoClient.close();
        if (server == null)
            return;

        try {
            server.stop();
        } catch (InterruptedException ignored) {
        }
    }

    protected CloseableHttpResponse testSimpleGetMethod(String url) throws Exception {
        return testSimpleGetMethod(url, "application/json", "application/json");
    }

    protected CloseableHttpResponse testSimpleGetMethod(String url, String contentType_header, String accept_header) throws Exception {
        HttpGet get = new HttpGet(url);
        get.setHeader(HttpHeaders.Names.CONTENT_TYPE, contentType_header);
        get.setHeader(HttpHeaders.Names.ACCEPT, accept_header);

        CloseableHttpClient httpClient = HttpClientBuilder.create().build();
        return httpClient.execute(get);
    }

    protected CloseableHttpResponse testSimplePostMethod(String url, String content) throws Exception {
        return testSimplePostMethod(url, content, "application/json", "application/json");
    }

    protected CloseableHttpResponse testSimplePostMethod(String url, String content, String contentType_header, String accept_header) throws Exception {
        HttpPost post = new HttpPost(url);
        post.setHeader(HttpHeaders.Names.CONTENT_TYPE, contentType_header);
        post.setHeader(HttpHeaders.Names.ACCEPT, accept_header);

        StringEntity entity = new StringEntity(content, ContentType.create("text/plain", "UTF-8"));
        post.setEntity(entity);

        CloseableHttpClient httpClient = HttpClientBuilder.create().build();
        return httpClient.execute(post);
    }

    protected CloseableHttpResponse testSimpleDeleteMethod(String url) throws Exception {
        return testSimpleDeleteMethod(url, "application/json", "application/json");
    }

    protected CloseableHttpResponse testSimpleDeleteMethod(String url, String contentType_header, String accept_header) throws Exception {
        HttpDelete delete = new HttpDelete(url);
        delete.setHeader(HttpHeaders.Names.CONTENT_TYPE, contentType_header);
        delete.setHeader(HttpHeaders.Names.ACCEPT, accept_header);

        CloseableHttpClient httpClient = HttpClientBuilder.create().build();
        return httpClient.execute(delete);
    }

    protected CloseableHttpResponse testSimplePutMethod(String url, String content) throws Exception {
        return testSimplePutMethod(url, content, "application/json", "application/json");
    }

    protected CloseableHttpResponse testSimplePutMethod(String url, String content, String contentType_header, String accept_header) throws Exception {
        HttpPut put = new HttpPut(url);
        put.setHeader(HttpHeaders.Names.CONTENT_TYPE, contentType_header);
        put.setHeader(HttpHeaders.Names.ACCEPT, accept_header);

        StringEntity entity = new StringEntity(content, ContentType.create("text/plain", "UTF-8"));
        put.setEntity(entity);

        CloseableHttpClient httpClient = HttpClientBuilder.create().build();
        return httpClient.execute(put);
    }

/*
    protected Map<String, CollectionResource> getCollectionResources(Resource resource) {
        TestResourceSink resourceSink = new TestResourceSink();

        if (resource instanceof CollectionResource) {
            ((CollectionResource)resource).writeMembers(resourceSink);
        } else if (resource instanceof ObjectResource) {
            ((ObjectResource)resource).writeMembers(resourceSink);
        } else {
            fail();
        }

        assertEquals(resourceSink.getCollectionResources().size(), resourceSink.getResources().size());
        return resourceSink.getCollectionResources();
    }

    protected Map<String, ObjectResource> getObjectResources(Resource resource) {
        TestResourceSink resourceSink = new TestResourceSink();

        if (resource instanceof CollectionResource) {
            ((CollectionResource)resource).writeMembers(resourceSink);
        } else if (resource instanceof ObjectResource) {
            ((ObjectResource)resource).writeMembers(resourceSink);
        } else {
            fail();
        }

        assertTrue(resourceSink.getObjectResources().size() == resourceSink.getResources().size());
        return resourceSink.getObjectResources();
    }


    protected Map<String, PropertyResource> getPropertyResources(Resource resource) {
        TestResourceSink resourceSink = new TestResourceSink();

        if (resource instanceof CollectionResource) {
            ((CollectionResource)resource).writeMembers(resourceSink);
        } else if (resource instanceof ObjectResource) {
            ((ObjectResource)resource).writeMembers(resourceSink);
        } else {
            fail();
        }

        assertTrue(resourceSink.getPropertyResources().size() == resourceSink.getResources().size());
        return resourceSink.getPropertyResources();
    }

    protected Map<String, Resource> getResources(Resource resource) {
        TestResourceSink resourceSink = new TestResourceSink();

        if (resource instanceof CollectionResource) {
            ((CollectionResource)resource).writeMembers(resourceSink);
        } else if (resource instanceof ObjectResource) {
            ((ObjectResource)resource).writeMembers(resourceSink);
        } else {
            fail();
        }

        return resourceSink.getResources();
    }

    protected class TestResourceSink implements ResourceSink {

        protected Map<String, Resource> resources = new HashMap<String, Resource>();
        protected Map<String, CollectionResource> collectionResources = new HashMap<String, CollectionResource>();
        protected Map<String, ObjectResource> objectResources = new HashMap<String, ObjectResource>();
        protected Map<String, PropertyResource> propertyResources = new HashMap<String, PropertyResource>();

        @Override
        public void close() {
            //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public void accept(Resource resource) {
            this.resources.put(resource.id(), resource);
            if (resource instanceof CollectionResource) {
                this.collectionResources.put(resource.id(), (CollectionResource) resource);
            } else if (resource instanceof ObjectResource) {
                this.objectResources.put(resource.id(), (ObjectResource) resource);
            } else if (resource instanceof PropertyResource) {
                this.propertyResources.put(resource.id(), (PropertyResource) resource);
            }
        }

        public Map<String, Resource> getResources() {
            return this.resources;
        }

        public Map<String, CollectionResource> getCollectionResources() {
            return this.collectionResources;
        }

        public Map<String, ObjectResource> getObjectResources() {
            return this.objectResources;
        }

        public Map<String, PropertyResource> getPropertyResources() {
            return this.propertyResources;
        }
    }

*/


}
