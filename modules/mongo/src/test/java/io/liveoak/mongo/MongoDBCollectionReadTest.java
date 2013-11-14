package io.liveoak.mongo;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.junit.Test;

import java.net.URLEncoder;

import static org.fest.assertions.Assertions.assertThat;
import static org.junit.Assert.*;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 */
public class MongoDBCollectionReadTest extends BaseMongoDBTest {

    @Test
    public void testGetStorageEmpty() throws Exception {
        db.dropDatabase(); //TODO: create a new DB here instead of dropping the old one
        assertThat( db.getCollectionNames() ).hasSize( 0 );

        CloseableHttpResponse response = testSimpleGetMethod(baseURL);
        // This should return an empty list since there are no collections
        assertThat( response.getStatusLine().getStatusCode() ).isEqualTo( 200 );

        // verify response
        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonNode = mapper.readTree(response.getEntity().getContent());

        assertThat( jsonNode.get( "id" ).asText() ).isEqualTo( "storage" );
        assertThat( jsonNode.get("type").asText() ).isEqualTo("collection");
        assertThat( jsonNode.get( "self" ).get( "href" ).asText() ).isEqualTo("/storage");
        assertThat( jsonNode.get( "_members" ) ).isNull();
    }

    @Test
    public void testGetStorageCollections() throws Exception {
        db.dropDatabase(); //TODO: create a new DB here instead of dropping the old one
        assertThat( db.getCollectionNames() ).hasSize(0);

        // create a couple of collections
        db.createCollection("collection1", new BasicDBObject());
        db.createCollection("collection2", new BasicDBObject());
        db.createCollection("collection3", new BasicDBObject());

        // check that the collections are there (Note: there is an internal index collection, so 4 instead of 3)
        assertThat( db.getCollectionNames() ).hasSize( 4 );

        CloseableHttpResponse response = testSimpleGetMethod(baseURL);
        // This should return an empty list since there are no collections
        assertThat( response.getStatusLine().getStatusCode() ).isEqualTo(200);

        // verify response
        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonNode = mapper.readTree(response.getEntity().getContent());

        assertThat(jsonNode.get("id").asText()).isEqualTo( "storage" );
        assertThat( jsonNode.get( "type" ).asText() ).isEqualTo( "collection" );
        assertThat( jsonNode.get( "self" ).get( "href" ).asText() ).isEqualTo( "/storage" );
        assertThat( jsonNode.get( "_members" ) ).isNotEmpty();

        assertThat( jsonNode.get( "_members" ).get( 0 ).get( "id" ).asText() ).isEqualTo( "collection1" );
        assertThat( jsonNode.get( "_members" ).get( 1 ).get( "id" ).asText() ).isEqualTo( "collection2" );
        assertThat( jsonNode.get( "_members" ).get( 2 ).get( "id" ).asText() ).isEqualTo( "collection3" );
    }

    @Test
    public void testGetEmptyCollection() throws Exception {
        String methodName = "testEmptyCollection";
        // check that the collection really is empty
        assertFalse(db.collectionExists(methodName));

        CloseableHttpResponse response = testSimpleGetMethod(baseURL + "/" + methodName);
        // collection does not exist yet, so should return 404
        assertEquals(404, response.getStatusLine().getStatusCode());

        db.createCollection(methodName, new BasicDBObject());
        response = testSimpleGetMethod(baseURL + "/" + methodName);
        assertEquals(200, response.getStatusLine().getStatusCode());

        // check that we get back an empty collection
        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonNode = mapper.readTree(response.getEntity().getContent());

        //assertEquals(4, jsonNode.size()); // id, _self, members, _subscriptions
        assertEquals(methodName, jsonNode.get("id").asText());
        assertThat( jsonNode.get( "self" ).get( "href" ).asText() ).isEqualTo("/storage/testEmptyCollection");
        assertNull(jsonNode.get("_members"));

        // check that the collection is still empty
        assertEquals(0, db.getCollection(methodName).getCount());
    }

    @Test
    public void testGetStorageCollectionsPagination() throws Exception {
        // DB db = mongoClient.getDB("testGetStorageCollectionsPagination");
        db.dropDatabase();
        assertEquals(0, db.getCollectionNames().size());
        // create a bunch of collections
        for (int i = 0; i < 1013; i++) {
            db.createCollection(String.format("collection%1$04d", i), new BasicDBObject("count", i));
        }
        // check that the collections are there (Note: there is an internal index collection, so 4 instead of 3)
        assertEquals(1014, db.getCollectionNames().size());

        // This should return 20 collections
        CloseableHttpResponse response = testSimpleGetMethod(baseURL + "?limit=20");

        assertEquals(200, response.getStatusLine().getStatusCode());

        // verify response
        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonNode = mapper.readTree(response.getEntity().getContent());
        JsonNode content = jsonNode.get("_members");

        assertEquals("20 collections limit check", 20, content.size());
        assertEquals("Check first collection is collection0000", "collection0000", content.get(0).get("id").asText());
        assertEquals("Check last collection is collection0019", "collection0019", content.get(19).get("id").asText());
        response.close();

        response = testSimpleGetMethod(baseURL + "?offset=1010&limit=20");
        // This should return 3 collections as a total number of them is 1013
        assertEquals(200, response.getStatusLine().getStatusCode());

        // verify response
        mapper = new ObjectMapper();
        jsonNode = mapper.readTree(response.getEntity().getContent());
        content = jsonNode.get("_members");

        assertEquals("Last 3 collections limit check", 3, content.size());
        assertEquals("Check first collection is collection1010", "collection1010", content.get(0).get("id").asText());
        assertEquals("Check last collection is collection1012", "collection1012", content.get(2).get("id").asText());

        // String entity = getEntityAsString(response.getEntity());
        // TODO: verify the entity that gets returned
        // System.out.println("ENTITY : " + entity);
    }


    @Test
    public void testGetStorageCollectionsQuery() throws Exception {

        DBCollection collection = db.getCollection("testQueryCollection");
        if (collection != null) {
            collection.drop();
        }
        collection = db.createCollection("testQueryCollection", new BasicDBObject("count", 0));

        // add a few people
        String [][] data = {
                {"John", "Doe", "US", "San Francisco"},
                {"Jane", "Doe", "US", "New York"},
                {"Hans", "Gruber", "DE", "Berlin"},
                {"Helga", "Schmidt", "DE", "Munich"},
                {"Francois", "Popo", "FR", "Marseille"},
                {"Jacqueline", "Coco", "FR", "Paris"}
        };

        addPeopleItems(collection, data);
        assertEquals("Data set up", 6, collection.count());

        // This should return 2 items
        CloseableHttpResponse response = testSimpleGetMethod(baseURL + "/testQueryCollection?expand=members&q=" + URLEncoder.encode("{lastName:{$gt:'E', $lt:'R'}}", "utf-8"));

        assertEquals(200, response.getStatusLine().getStatusCode());

        // verify response
        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonNode = mapper.readTree(response.getEntity().getContent());
        JsonNode content = jsonNode.get("_members");

        System.err.println( content );

        assertEquals("Number of items check", 2, content.size());
        assertEquals("Check first item is Hans", "Hans", content.get(0).get("name").asText());
        assertEquals("Check last item is Francois", "Francois", content.get(1).get("name").asText());
        response.close();


        // This should return 2 items
        response = testSimpleGetMethod(baseURL + "/testQueryCollection?expand=members&q=" + URLEncoder.encode("{lastName:'Doe'}", "utf-8"));

        assertEquals(200, response.getStatusLine().getStatusCode());

        // verify response
        mapper = new ObjectMapper();
        jsonNode = mapper.readTree(response.getEntity().getContent());
        content = jsonNode.get("_members");

        assertEquals("Number of items check", 2, content.size());
        assertEquals("Check first item is John", "John", content.get(0).get("name").asText());
        assertEquals("Check last item is Jane", "Jane", content.get(1).get("name").asText());
        response.close();

    }

    private void addPeopleItems(DBCollection collection, String[][] data) {
        for (String[] rec: data) {
            BasicDBObject obj = new BasicDBObject();
            obj.put("name", rec[0]);
            obj.put("lastName", rec[1]);
            obj.put("country", rec[2]);
            obj.put("city", rec[3]);

            collection.insert(obj);

            System.err.println( "INSERT: " + obj );
        }
    }
}
