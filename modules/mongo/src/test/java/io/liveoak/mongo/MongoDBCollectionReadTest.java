package io.liveoak.mongo;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.BasicDBObject;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

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

        System.err.println( jsonNode );

        assertThat(jsonNode.get("id").asText()).isEqualTo( "storage" );
        assertThat( jsonNode.get( "type" ).asText() ).isEqualTo( "collection" );
        assertThat( jsonNode.get( "self" ).get( "href" ).asText() ).isEqualTo( "/storage" );
        assertThat( jsonNode.get( "_members" ) ).isNotEmpty();

        assertThat( jsonNode.get( "_members" ).get( 0 ).get( "id" ).asText() ).isEqualTo( "collection1" );
        assertThat( jsonNode.get( "_members" ).get( 1 ).get( "id" ).asText() ).isEqualTo( "collection2" );
        assertThat( jsonNode.get( "_members" ).get( 2 ).get( "id" ).asText() ).isEqualTo( "collection3" );
    }
}
