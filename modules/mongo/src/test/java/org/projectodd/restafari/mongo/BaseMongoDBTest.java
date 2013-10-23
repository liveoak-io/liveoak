package org.projectodd.restafari.mongo;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.MongoClient;
import io.netty.handler.codec.http.HttpHeaders;
import org.apache.http.client.methods.*;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.projectodd.restafari.container.DefaultContainer;
import org.projectodd.restafari.container.SimpleConfig;
import org.projectodd.restafari.container.UnsecureServer;

import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

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
        String database = System.getProperty("mongo.db", "db" + UUID.randomUUID());
        Integer port = new Integer(System.getProperty("mongo.port", "27017"));
        String host = System.getProperty("mongo.host", "localhost");

        // configure the mongo controller
        SimpleConfig config = new SimpleConfig();
        config.put("db", database);
        config.put("port", port);
        config.put("host", host);

        DefaultContainer container = new DefaultContainer();
        container.registerResource(new MongoDBResource(TYPE), config);

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

    @Test
    public void testGetStorageEmpty() throws Exception {
        //DB db = mongoClient.getDB("testGetStorageEmpty");
        db.dropDatabase(); //TODO: create a new DB here instead of dropping the old one
        assertEquals(0, db.getCollectionNames().size());

        CloseableHttpResponse response = testSimpleGetMethod(baseURL);
        // This should return an empty list since there are no collections
        assertEquals(200, response.getStatusLine().getStatusCode());

        // verify response
        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonNode = mapper.readTree(response.getEntity().getContent());

        assertEquals(3, jsonNode.size());  // id, _self, members
        assertEquals("storage", jsonNode.get("id").asText());
        assertEquals("/storage", jsonNode.get("_self").get("href").asText());
        assertEquals("collection", jsonNode.get("_self").get("type").asText());
        assertEquals("[]", jsonNode.get("content").toString());
    }

    @Test
    public void testGetStorageCollections() throws Exception {
        //DB db = mongoClient.getDB("testGetStorageCollections");
        db.dropDatabase(); //TODO: create a new DB here instead of dropping the old one
        assertEquals(0, db.getCollectionNames().size());
        // create a couple of collections
        db.createCollection("collection1", new BasicDBObject());
        db.createCollection("collection2", new BasicDBObject());
        db.createCollection("collection3", new BasicDBObject());
        // check that the collections are there (Note: there is an internal index collection, so 4 instead of 3)
        assertEquals(4, db.getCollectionNames().size());


        CloseableHttpResponse response = testSimpleGetMethod(baseURL);
        // This should return an empty list since there are no collections
        assertEquals(200, response.getStatusLine().getStatusCode());

        // verify response
        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonNode = mapper.readTree(response.getEntity().getContent());

        assertEquals(3, jsonNode.size());  // id, _self, members
        assertEquals("storage", jsonNode.get("id").asText());
        assertEquals("/storage", jsonNode.get("_self").get("href").asText());
        assertEquals("collection", jsonNode.get("_self").get("type").asText());
        assertEquals("{\"id\":\"collection1\",\"_self\":{\"href\":\"/storage/collection1\",\"type\":\"collection\"}}",
                jsonNode.get("content").get(0).toString());
        assertEquals("{\"id\":\"collection2\",\"_self\":{\"href\":\"/storage/collection2\",\"type\":\"collection\"}}",
                jsonNode.get("content").get(1).toString());
        assertEquals("{\"id\":\"collection3\",\"_self\":{\"href\":\"/storage/collection3\",\"type\":\"collection\"}}",
                jsonNode.get("content").get(2).toString());
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
}
