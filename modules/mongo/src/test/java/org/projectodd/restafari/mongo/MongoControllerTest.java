package org.projectodd.restafari.mongo;

import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.net.InetAddress;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.*;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.bson.types.ObjectId;
import org.junit.*;
import org.projectodd.restafari.container.Container;
import org.projectodd.restafari.container.SimpleConfig;
import org.projectodd.restafari.container.UnsecureServer;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;

import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.handler.codec.http.HttpHeaders;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 */
public class MongoControllerTest {

    protected static UnsecureServer server;
    protected static MongoClient mongoClient;
    protected static DB db;

    protected static String baseURL;
    protected static final String TYPE = "storage";

    @BeforeClass
    public static void init() throws Exception
    {
        String database = System.getProperty("mongo.db", "MongoControllerTest_" + Math.random());
        Integer port = new Integer(System.getProperty("mongo.port", "27017"));
        String host = System.getProperty("mongo.host", "localhost");

        // configure the mongo controller
        SimpleConfig config = new SimpleConfig();
        config.put("db", database);
        config.put("port", port);
        config.put("host", host);

        Container container = new Container();
        container.registerResourceController("storage", new MongoController(), config);
        server = new UnsecureServer(container, InetAddress.getByName("localhost"), 8080, new NioEventLoopGroup());
        server.start();

        baseURL = "http://localhost:8080/" + TYPE;

        // configure a local mongo client to verify the data methods
        mongoClient = new MongoClient(host, port);
        db = mongoClient.getDB(database);
    }

    @AfterClass
    public static void destroy() throws Exception
    {
        if (server != null) // TODO: should there be a server.status or server.state?
        {
            server.stop();
        }
    }

    @Before
    public void preTestMethod() throws Exception {
        // TODO: if we use the method name as the collection name, we might not need to do this here
        // assertTrue("Test Database must be empty before starting the tests", db.getCollectionNames().isEmpty());
    }

    @After
    public void postTestMethod() throws Exception {
        // TODO: if we use the method name as the collection name, we might not need to do this here
        // db.dropDatabase();
    }

    @Test
    public void testGetStorageEmpty() throws Exception {
        DB db = mongoClient.getDB("testGetStorageEmpty");
        assertEquals(0, db.getCollectionNames().size());

        CloseableHttpResponse response = testSimpleGetMethod(baseURL);
        // This should return an empty list since there are no collections
        assertEquals(200, response.getStatusLine().getStatusCode());
        assertEquals("[]", getEntityAsString(response.getEntity()));
    }

    @Test
    public void testGetStorageCollections() throws Exception {
        // DB db = mongoClient.getDB("testGetStorageCollections");
        db.dropDatabase();
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

        // String entity = getEntityAsString(response.getEntity());
        // TODO: verify the entity that gets returned
        // System.out.println("ENTITY : " + entity);
    }

    @Test
    public void testGetStorageCollectionsPagination() throws Exception {
        // DB db = mongoClient.getDB("testGetStorageCollectionsPagination");
        db.dropDatabase();
        assertEquals(0, db.getCollectionNames().size());
        // create a bunch of collections
        for (int i = 0; i < 1013; i++)
        {
            db.createCollection("collection" + i, new BasicDBObject("count", i));
        }
        // check that the collections are there (Note: there is an internal index collection, so 4 instead of 3)
        assertEquals(1014, db.getCollectionNames().size());

        CloseableHttpResponse response = testSimpleGetMethod(baseURL);
        // This should return an empty list since there are no collections
        assertEquals(200, response.getStatusLine().getStatusCode());

        // String entity = getEntityAsString(response.getEntity());
        // TODO: verify the entity that gets returned
        // System.out.println("ENTITY : " + entity);
    }

    @Test
    public void testGetEmptyCollection() throws Exception {
        String methodName = "testEmtpyCollection";
        // check that the collection really is empty
        assertFalse(db.getCollectionNames().contains(methodName));

        CloseableHttpResponse response = testSimpleGetMethod(baseURL + "/" + methodName);
        assertEquals(200, response.getStatusLine().getStatusCode());
        // check that we get back an empty collection
        assertEquals("[]", getEntityAsString(response.getEntity()));

        // check that the collection is still empty
        assertEquals(0, db.getCollection(methodName).getCount());
    }

    @Test
    public void testSimpleGet() throws Exception {
        String methodName = "testSimpleGet";
        assertEquals(0, db.getCollection(methodName).getCount());

        // create the object using the mongo driver directly
        BasicDBObject object = new BasicDBObject();
        object.append("foo", "bar");
        db.getCollection(methodName).insert(object);
        assertEquals(1, db.getCollection(methodName).getCount());
        String id = object.getObjectId("_id").toString();

        CloseableHttpResponse response = testSimpleGetMethod(baseURL + "/" + methodName + "/" + id);
        assertEquals(200, response.getStatusLine().getStatusCode());

        // verify response
        String responseEntity = getEntityAsString(response.getEntity());
        assertEquals(id, getId(responseEntity.getBytes()));
        assertEquals("{\"id\":\"" + id + "\",\"foo\":\"bar\"}", responseEntity);
    }

    @Test
    public void testGetInvalidId() throws Exception {
        String methodName = "testGetInvalidId";
        assertEquals(0, db.getCollection(methodName).getCount());

        CloseableHttpResponse response = testSimpleGetMethod(baseURL + "/" + methodName + "/foobar123");
        assertEquals(404, response.getStatusLine().getStatusCode());
    }

    @Test
    public void testGetNonExistantId() throws Exception {
        String methodName = "testGetNonExistantId";
        assertEquals(0, db.getCollection(methodName).getCount());

        ObjectId id = new ObjectId();

        CloseableHttpResponse response = testSimpleGetMethod(baseURL + "/" + methodName + "/" + id);
        assertEquals(404, response.getStatusLine().getStatusCode());
    }

    @Test
    public void testSimpleCreate() throws Exception {
        String methodName = "testSimpleCreate";
        assertEquals(0, db.getCollection(methodName).getCount());

        CloseableHttpResponse response = testSimplePostMethod(baseURL + "/" + methodName, "{\"foo\":\"bar\"}");
        assertEquals(201, response.getStatusLine().getStatusCode());

        // verify response
        String responseEntity = getEntityAsString(response.getEntity());
        String id = getId(responseEntity.getBytes());
        assertEquals("{\"id\":\"" + id + "\",\"foo\":\"bar\"}", responseEntity);

        // verify what is stored in the mongo db
        assertEquals(1, db.getCollection(methodName).getCount());
        DBObject dbObject = db.getCollection(methodName).findOne();
        assertEquals("bar", dbObject.get("foo"));
        assertEquals(new ObjectId(id), dbObject.get("_id"));
    }

    @Test
    public void testSimpleDelete() throws Exception {
        String methodName = "testSimpleDelete";
        assertEquals(0, db.getCollection(methodName).getCount());

        // create the object using the mongo driver directly
        BasicDBObject object = new BasicDBObject();
        object.append("foo", "bar");
        db.getCollection(methodName).insert(object);
        assertEquals(1, db.getCollection(methodName).getCount());
        String id = object.getObjectId("_id").toString();

        // now delete the object
        CloseableHttpResponse response = testSimpleDeleteMethod(baseURL + "/" + methodName + "/" + id);
        assertEquals(200, response.getStatusLine().getStatusCode());
        assertEquals(0, db.getCollection(methodName).getCount());
    }

    @Test
    public void testDeleteNonExistantCollection() throws Exception {
        String methodName = "testDeleteNonExistantCollection";
        assertEquals(0, db.getCollection(methodName).getCount());

        CloseableHttpResponse response = testSimpleDeleteMethod(baseURL + "/" + methodName);
        assertEquals(404, response.getStatusLine().getStatusCode());
    }

    @Test
    public void testDeleteCollection() throws Exception {
        String methodName = "testDeleteCollection";
        assertFalse(db.getCollectionNames().contains(methodName));

        // create the collection
        db.createCollection(methodName, new BasicDBObject());

        assertTrue(db.getCollectionNames().contains(methodName));

        CloseableHttpResponse response = testSimpleDeleteMethod(baseURL + "/" + methodName);
        assertEquals(200, response.getStatusLine().getStatusCode());

        // check that it was actually deleted
        assertFalse(db.getCollectionNames().contains(methodName));
    }

    @Test
    public void testDeleteInvalidId() throws Exception {
        String methodName = "testDeleteInvalidId";
        assertEquals(0, db.getCollection(methodName).getCount());

        CloseableHttpResponse response = testSimpleDeleteMethod(baseURL + "/" + methodName + "/foobar123");
        assertEquals(404, response.getStatusLine().getStatusCode());
    }

    @Test
    public void testDeleteNonExistantId() throws Exception {
        String methodName = "testDeleteNonExistantId";
        assertEquals(0, db.getCollection(methodName).getCount());

        ObjectId id = new ObjectId();

        CloseableHttpResponse response = testSimpleDeleteMethod(baseURL + "/" + methodName + "/" + id.toString());
        assertEquals(404, response.getStatusLine().getStatusCode());
    }

    @Test
    public void testSimpleUpdate() throws Exception {
        String methodName = "testSimpleDelete";
        assertEquals(0, db.getCollection(methodName).getCount());

        // create the object using the mongo driver directly
        BasicDBObject object = new BasicDBObject();
        object.append("foo", "bar");
        db.getCollection(methodName).insert(object);
        assertEquals(1, db.getCollection(methodName).getCount());
        String id = object.getObjectId("_id").toString();

        // now update the object
        CloseableHttpResponse response = testSimplePutMethod(baseURL + "/" + methodName + "/" + id, "{\"foo\":\"baz\"}");
        String responseEntity = getEntityAsString(response.getEntity());
        assertEquals(200, response.getStatusLine().getStatusCode());
        assertEquals("{\"id\":\"" + id + "\",\"foo\":\"baz\"}", responseEntity);

        assertEquals(1, db.getCollection(methodName).getCount());
        DBObject dbObject = db.getCollection(methodName).findOne();
        assertEquals("baz", dbObject.get("foo"));
        assertEquals(new ObjectId(id), dbObject.get("_id"));
    }

    protected CloseableHttpResponse testSimpleGetMethod(String url) throws Exception {
        return testSimpleGetMethod(url, "application/json", "application/json");
    }

    protected CloseableHttpResponse testSimpleGetMethod(String url, String contentType_header, String accept_header) throws Exception
    {
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

    protected String getEntityAsString(HttpEntity entity) throws Exception
    {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        entity.writeTo(outputStream);
        String result = new String(outputStream.toByteArray());
        outputStream.close();

        return result;
    }

    public String getId(byte[] entity) throws Exception {
        JsonFactory factory = new JsonFactory();
        JsonParser parser = factory.createParser(entity);

        String id = null;
        while (parser.nextToken() != JsonToken.END_OBJECT)
        {
            String name = parser.getCurrentName();
            if (name != null && name.equals("id"))
            {
                parser.nextToken();
                id = parser.getValueAsString();
                break;
            }
        }
        if (id == null)
        {
            fail("Could not find ID from the response");
        }

        return id;
    }

}
