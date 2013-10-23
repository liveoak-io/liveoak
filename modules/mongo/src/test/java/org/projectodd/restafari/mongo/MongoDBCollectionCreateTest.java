package org.projectodd.restafari.mongo;

import static org.fest.assertions.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.junit.Test;
import org.projectodd.restafari.container.codec.DefaultCollectionResourceState;
import org.projectodd.restafari.container.codec.DefaultObjectResourceState;
import org.projectodd.restafari.spi.resource.Resource;
import org.projectodd.restafari.spi.state.ObjectResourceState;

import static org.junit.Assert.*;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 */
public class MongoDBCollectionCreateTest extends BaseMongoDBTest{

//    @Test
//    public void testSimple() throws Exception {
//        ObjectResourceState state = new DefaultObjectResourceState();
//        state.id("movies");
//        state.addProperty("movies", new DefaultCollectionResourceState()); //TODO: figure why this is here.
//
//        //check that the collection doesn't currently exist in mongo
//        assertFalse(db.collectionExists("movies"));
//
//        Resource createdResource = connector.create("/storage", state);
//
//        //check returned resource is correct
//        assertThat(createdResource).isNotNull();
//        assertThat(createdResource).isInstanceOf(MongoCollectionResource.class);
//        assertThat(createdResource.id()).isEqualTo("movies");
//
//        //check that that data is what is expected in mongo
//        assertTrue(db.collectionExists("movies"));
//        assertEquals(0, db.getCollection("movies").getCount());
//
////        TODO: remove this, testing reads should be done separately from creates
////        Resource movies = connector.read("/storage/movies");
////        assertThat( movies ).isNotNull();
////        assertThat( movies ).isInstanceOf( MongoCollectionResource.class );
////        assertThat( movies.id() ).isEqualTo( "movies" );
//    }

//    @Test
//    public void testCreateCollectionWithoutID() throws Exception {
//        DefaultCollectionResourceState state = new DefaultCollectionResourceState();
//
//        Resource createdResource = connector.create("/storage", state);
//
//        //check returned resource is correct
//        assertThat(createdResource).isNotNull();
//        assertThat(createdResource).isInstanceOf(MongoCollectionResource.class);
//        String id = createdResource.id();
//        assertNotNull(createdResource.id());
//
//        //check that that data is what is expected in mongo
//        assertTrue(db.collectionExists(id));
//        assertEquals(0, db.getCollection(id).getCount());
//    }

    @Test
    public void testCreateCollection() throws Exception {
        //DB db = mongoClient.getDB("testGetStorageEmpty");
        db.dropDatabase(); //TODO: create a new DB here instead of dropping the old one
        assertEquals(0, db.getCollectionNames().size());

        CloseableHttpResponse response = testSimplePostMethod(baseURL, "{\"id\":\"testCollection\"}");
        assertEquals(201, response.getStatusLine().getStatusCode());

        // verify response
        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonNode = mapper.readTree(response.getEntity().getContent());
        assertEquals("testCollection", jsonNode.get("id").asText());
        assertNotNull(jsonNode.get("_self"));
        assertEquals("[]", jsonNode.get("content").toString());

        // verify whats in mongodb
        assertTrue(db.collectionExists("testCollection"));
        assertTrue(db.getCollection("testCollection").count() == 0);
    }

    @Test
    public void testCreateCollectionNoId() throws Exception {
        //DB db = mongoClient.getDB("testGetStorageEmpty");
        db.dropDatabase(); //TODO: create a new DB here instead of dropping the old one
        assertEquals(0, db.getCollectionNames().size());

        CloseableHttpResponse response = testSimplePostMethod(baseURL, "");
        assertEquals(201, response.getStatusLine().getStatusCode());

        // verify response
        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonNode = mapper.readTree(response.getEntity().getContent());
        assertNotNull(jsonNode.get("id"));
        assertNotNull(jsonNode.get("_self"));
        assertEquals("[]", jsonNode.get("content").toString());

        //verify whats in mongodb
        String id = jsonNode.get("id").asText();
        assertTrue(db.collectionExists(id));
        assertTrue(db.getCollection(id).count() == 0);
    }
}
