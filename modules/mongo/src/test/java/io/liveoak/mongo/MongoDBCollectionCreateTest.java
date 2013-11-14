package io.liveoak.mongo;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.BasicDBObject;
import io.liveoak.container.codec.DefaultResourceState;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.ResourceAlreadyExistsException;
import io.liveoak.spi.state.ResourceState;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.fest.assertions.Fail;
import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 */
public class MongoDBCollectionCreateTest extends NewBaseMongoDBTest {

    @Test
    public void testCreateCollection() throws Exception {
        //DB db = mongoClient.getDB("testGetStorageEmpty");
        db.dropDatabase(); //TODO: create a new DB here instead of dropping the old one ?

        assertThat(db.getCollectionNames()).hasSize(0);

        ResourceState state = new DefaultResourceState("testCollection");

        ResourceState response = connector.create(new RequestContext.Builder().build(), BASEPATH, state);

        // verify response
        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo("testCollection");
        assertThat(response.getProperty("type")).isEqualTo("collection");
        assertThat(response.members()).isEmpty();

        // verify whats in mongodb
        assertThat(db.collectionExists("testCollection")).isTrue();
        assertThat(db.getCollection("testCollection").count()).isEqualTo(0);
    }

    @Test
    public void testCreateCollectionNoId() throws Exception {
        //DB db = mongoClient.getDB("testGetStorageEmpty");
        db.dropDatabase(); //TODO: create a new DB here instead of dropping the old one ?
        assertThat(db.getCollectionNames()).hasSize(0);

        ResourceState response = connector.create(new RequestContext.Builder().build(), BASEPATH, new DefaultResourceState());

        //verfiy response
        assertThat(response).isNotNull();
        assertThat(response.id()).isNotNull();
        assertThat(response.getProperty("type")).isEqualTo("collection");
        assertThat(response.members()).isEmpty();
        String id = response.id();

        //verify whats in mongodb
        assertThat(db.collectionExists(id)).isTrue();
        assertThat(db.getCollection(id).count()).isEqualTo(0);
    }

    @Test
    public void testCreateAlreadyExisting() throws Exception {
        db.dropDatabase(); //TODO: create a new DB here instead of dropping the old one ?
        assertThat(db.collectionExists("foobar")).isFalse();
        //create a collection
        db.createCollection("foobar", new BasicDBObject());
        assertThat(db.collectionExists("foobar")).isTrue();

        try {
            ResourceState response = connector.create(new RequestContext.Builder().build(), BASEPATH, new DefaultResourceState("foobar"));
            Fail.fail("shouldn't get here");
        } catch (ResourceAlreadyExistsException e) {
            //expected
        }

    }
}
