package org.projectodd.restafari.mongo;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.junit.Test;
import org.projectodd.restafari.spi.ResourceNotFoundException;
import org.projectodd.restafari.spi.resource.Resource;
import org.projectodd.restafari.spi.resource.async.CollectionResource;
import org.projectodd.restafari.spi.resource.async.ObjectResource;

import java.util.Map;

import static org.fest.assertions.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 */
public class MongoDBCollectionReadTest extends BaseMongoDBTest {

//    @Test
//    public void testRootFound() throws Exception {
//        Resource result = connector.read("/storage");
//        assertThat(result).isNotNull();
//        assertThat(result).isInstanceOf(MongoResource.class);
//    }
//
//    @Test
//    public void testUncreatedCollectionNotFound() throws Exception {
//        try {
//            connector.read("/storage/movies");
//            fail( "shouldn't get here" );
//        } catch (ResourceNotFoundException e) {
//            assertThat( e.path() ).isEqualTo( "/storage/movies" );
//        }
//    }
//
//    @Test
//    public void testEmpty() throws Exception {
//        CollectionResource result = (CollectionResource)connector.read("/storage");
//
//        assertThat(result.id()).isEqualTo("storage");
//
//        TestResourceSink sink = new TestResourceSink();
//        result.writeMembers(sink);
//        assertThat(sink.getResources().size()).isEqualTo(0);
//    }
//
//    @Test
//    public void testGetStoredCollections() throws Exception {
//        db.dropDatabase(); //TODO: create a new DB here instead of dropping the old one
//        assertEquals(0, db.getCollectionNames().size());
//
//        // create a couple of collections
//        db.createCollection("collection1", new BasicDBObject());
//
//        DBCollection dbCollection2 = db.createCollection("collection2", new BasicDBObject());
//        dbCollection2.insert(new BasicDBObject("foo", "bar"));
//        dbCollection2.insert(new BasicDBObject("hello", "world").append("foo", "baz"));
//
//        db.createCollection("collection3", new BasicDBObject());
//        // check that the collections are there (Note: there is an internal index collection, so 4 instead of 3)
//        assertEquals(4, db.getCollectionNames().size());
//
//        CollectionResource result = (CollectionResource)connector.read("/storage");
//        assertThat(result.id()).isEqualTo("storage");
//
//
//        Map<String, CollectionResource> resources = getCollectionResources(result);
//
//        assertThat(resources.size()).isEqualTo(3); //Make sure we are not getting back system.indexes
//        assertThat(resources.get("collection1")).isNotNull();
//        assertThat(resources.get("collection2")).isNotNull();
//        assertThat(resources.get("collection3")).isNotNull();
//
//        assertThat(getResources(resources.get("collection1")).size()).isEqualTo(0);
//        assertThat(getResources(resources.get("collection3")).size()).isEqualTo(0);
//
//        Map<String, ObjectResource> c2ObjectResource = getObjectResources(resources.get("collection2"));
//        assertThat(c2ObjectResource.size()).isEqualTo(2);
//        for (ObjectResource oResource: c2ObjectResource.values()) {
//            System.out.println(getPropertyResources(oResource));
//            System.out.println(oResource.id());
//        }
//
//
//    }

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
        assertEquals("{\"id\":\"collection1\",\"_self\":{\"href\":\"/storage/collection1\",\"type\":\"collection\"},\"content\":[]}",
                jsonNode.get("content").get(0).toString());
        assertEquals("{\"id\":\"collection2\",\"_self\":{\"href\":\"/storage/collection2\",\"type\":\"collection\"},\"content\":[]}",
                jsonNode.get("content").get(1).toString());
        assertEquals("{\"id\":\"collection3\",\"_self\":{\"href\":\"/storage/collection3\",\"type\":\"collection\"},\"content\":[]}",
                jsonNode.get("content").get(2).toString());
    }
}
