package io.liveoak.mongo;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 */
public class MongoDBCollectionCreateTest extends BaseMongoDBTest {

    @Test
    public void testCreateCollection() throws Exception {
        //DB db = mongoClient.getDB("testGetStorageEmpty");
        db.dropDatabase(); //TODO: create a new DB here instead of dropping the old one

        assertThat(db.getCollectionNames()).hasSize(0);

        CloseableHttpResponse response = testSimplePostMethod(baseURL, "{\"id\":\"testCollection\"}");
        assertThat(response.getStatusLine().getStatusCode()).isEqualTo(201);

        // verify response
        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonNode = mapper.readTree(response.getEntity().getContent());
        assertThat(jsonNode.get("id").asText()).isEqualTo("testCollection");
        assertThat(jsonNode.get("self")).isNotNull();
        assertThat(jsonNode.get("members")).isNull();

        // verify whats in mongodb
        assertThat(db.collectionExists("testCollection")).isTrue();
        assertThat(db.getCollection("testCollection").count()).isEqualTo(0);
    }

    @Test
    public void testCreateCollectionNoId() throws Exception {
        //DB db = mongoClient.getDB("testGetStorageEmpty");
        db.dropDatabase(); //TODO: create a new DB here instead of dropping the old one
        assertThat(db.getCollectionNames()).hasSize(0);

        CloseableHttpResponse response = testSimplePostMethod(baseURL, "");
        assertThat(response.getStatusLine().getStatusCode()).isEqualTo(201);

        // verify response
        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonNode = mapper.readTree(response.getEntity().getContent());
        assertThat(jsonNode.get("id").asText()).isNotNull();
        assertThat(jsonNode.get("self")).isNotNull();
        assertThat(jsonNode.get("members")).isNull();

        //verify whats in mongodb
        String id = jsonNode.get("id").asText();

        assertThat(db.collectionExists(id)).isTrue();
        assertThat(db.getCollection(id).count()).isEqualTo(0);
    }
}
