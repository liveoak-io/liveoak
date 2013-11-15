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
import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.UUID;

import static org.fest.assertions.Assertions.assertThat;
import static org.junit.Assert.assertFalse;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 */
public class MongoDBResourceTest {

    //NOTE: will be soon be removed. Do not add any test classes here anymore //

    private static UnsecureServer server;
    protected static MongoClient mongoClient;
    protected static DB db;

    protected static String baseURL;
    protected static final String TYPE = "storage";

    @BeforeClass
    public static void init() throws Exception {
        String database = System.getProperty("mongo.db", "MongoControllerTest_" + UUID.randomUUID());
        Integer port = new Integer(System.getProperty("mongo.port", "27017"));
        String host = System.getProperty("mongo.host", "localhost");

        // configure the mongo controller
        SimpleConfig config = new SimpleConfig();
        config.put("db", database);
        config.put("port", port);
        config.put("host", host);

        DefaultContainer container = new DefaultContainer();
        container.registerResource(new RootMongoResource(TYPE), config);

        server = new UnsecureServer(container, "localhost", 8080);
        server.start();

        baseURL = "http://localhost:8080/" + TYPE;

        // configure a local mongo client to verify the data methods
        mongoClient = new MongoClient(host, port);
        db = mongoClient.getDB(database);
    }

    @AfterClass
    public static void dispose() {
        if (server == null)
            return;

        try {
            server.stop();
        } catch (InterruptedException ignored) {
        }
    }

    @Test
    public void testRootFound() throws Exception {

        RequestConfig cconfig = RequestConfig.custom().setSocketTimeout(500000).build();
        CloseableHttpClient httpClient = HttpClients.custom().setDefaultRequestConfig(cconfig).build();

        HttpGet get = new HttpGet("http://localhost:8080/storage");
        get.addHeader("Accept", "application/json");

        try {
            System.err.println("DO GET");
            CloseableHttpResponse result = httpClient.execute(get);
            System.err.println("=============>>>");
            System.err.println(result);

            HttpEntity entity = result.getEntity();
            if (entity.getContentLength() > 0) {
                entity.writeTo(System.err);
            }
            System.err.println("\n<<<=============");
            assertThat(result.getStatusLine().getStatusCode()).isEqualTo(200);

        } finally {
            httpClient.close();
        }
    }

    @Test
    public void testUncreatedCollectionNotFound() throws Exception {
        db.getCollection("movies").drop();
        assertFalse(db.collectionExists("movies"));

        CloseableHttpClient httpClient = HttpClientBuilder.create().build();
        HttpGet get = new HttpGet("http://localhost:8080/storage/movies");
        get.addHeader("Accept", "application/json");

        try {
            System.err.println("DO GET");
            CloseableHttpResponse result = httpClient.execute(get);
            System.err.println("=============>>>");
            System.err.println(result);
            result.getEntity().writeTo(System.err);
            System.err.println("\n<<<=============");
            assertThat(result.getStatusLine().getStatusCode()).isEqualTo(404);

        } finally {
            httpClient.close();
        }
    }

    @Test
    public void testCreateCollection() throws Exception {

        CloseableHttpClient httpClient = HttpClientBuilder.create().build();

        HttpPut put = new HttpPut("http://localhost:8080/storage/movies");
        put.setEntity(new StringEntity("{ \"id\": \"movies\", \"members\": [] }"));
        put.setHeader("Content-Type", "application/json");

        CloseableHttpResponse result = httpClient.execute(put);
        System.err.println("=============>>>");
        System.err.println(result);
        result.getEntity().writeTo(System.err);
        System.err.println("\n<<<=============");
        assertThat(result.getStatusLine().getStatusCode()).isEqualTo(201);


        HttpGet get = new HttpGet("http://localhost:8080/storage/movies");
        get.addHeader("Accept", "application/json");

        try {
            System.err.println("DO GET");
            result = httpClient.execute(get);
            System.err.println("=============>>>");
            System.err.println(result);
            result.getEntity().writeTo(System.err);
            System.err.println("\n<<<=============");
            assertThat(result.getStatusLine().getStatusCode()).isEqualTo(200);

        } finally {
            httpClient.close();
        }
    }
}
