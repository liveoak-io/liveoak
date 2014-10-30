package io.liveoak.mongo;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import io.liveoak.common.codec.DefaultResourceState;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.exceptions.NotAcceptableException;
import io.liveoak.spi.exceptions.ResourceAlreadyExistsException;
import io.liveoak.spi.state.ResourceState;
import org.fest.assertions.Fail;
import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 */
public class MongoDBCollectionUpdateTest extends BaseMongoDBTest {

    @Test
    public void updateNoChange() throws Exception {
        db.dropDatabase(); // TODO: create a new DB here instead of dropping the old one
        assertThat(db.getCollectionNames()).hasSize(0);

        // create a collection
        DBCollection collection = db.createCollection("foo", new BasicDBObject());
        DBObject dbObject = new BasicDBObject("_id", "testObject");
        dbObject.put("hello", "world");
        collection.insert(dbObject);

        // check that the collection is there (Note: there is an internal index collection, so one more than expected)
        assertThat(db.getCollectionNames()).hasSize(2);
        assertThat(db.getCollection("foo").count() == 1);

        // set the update resource state to include the same id value
        ResourceState updateResourceState = new DefaultResourceState("foo");

        ResourceState result = client.update(new RequestContext.Builder().build(), "/testApp/" + BASEPATH + "/foo", updateResourceState);

        // verify the result
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo("foo");
        assertThat(result.getProperty("count")).isEqualTo(1L);

        // check in mongo directly
        assertThat(db.collectionExists("foo"));
        assertThat(db.getCollection("foo").count()).isEqualTo(1L);
        assertThat(db.getCollection("foo").findOne().get("hello")).isEqualTo("world");
    }

    @Test
    public void updateEmpty() throws Exception {
        db.dropDatabase(); // TODO: create a new DB here instead of dropping the old one
        assertThat(db.getCollectionNames()).hasSize(0);

        // create a collection
        DBCollection collection = db.createCollection("foo", new BasicDBObject());
        DBObject dbObject = new BasicDBObject("_id", "testObject");
        dbObject.put("hello", "world");
        collection.insert(dbObject);

        // check that the collection is there (Note: there is an internal index collection, so one more than expected)
        assertThat(db.getCollectionNames()).hasSize(2);
        assertThat(db.getCollection("foo").count() == 1);

        // set the update resource state to be empty
        ResourceState updateResourceState = new DefaultResourceState();

        ResourceState result = client.update(new RequestContext.Builder().build(), "/testApp/" + BASEPATH + "/foo", updateResourceState);

        // verify the result
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo("foo");
        assertThat(result.getProperty("count")).isEqualTo(1L);

        // check in mongo directly
        assertThat(db.collectionExists("foo"));
        assertThat(db.getCollection("foo").count()).isEqualTo(1L);
        assertThat(db.getCollection("foo").findOne().get("hello")).isEqualTo("world");
    }

    @Test
    public void renameCollection() throws Exception {
        db.dropDatabase(); // TODO: create a new DB here instead of dropping the old one
        assertThat(db.getCollectionNames()).hasSize(0);

        // create a collection
        DBCollection collection = db.createCollection("foo", new BasicDBObject());
        DBObject dbObject = new BasicDBObject("_id", "testObject");
        dbObject.put("hello", "world");
        collection.insert(dbObject);

        // check that the collection is there (Note: there is an internal index collection, so one more than expected)
        assertThat(db.getCollectionNames()).hasSize(2);
        assertThat(db.getCollection("foo").count() == 1);

        // set the update resource state to include a new id value
        ResourceState updateResourceState = new DefaultResourceState("bar");

        ResourceState result = client.update(new RequestContext.Builder().build(), "/testApp/" + BASEPATH + "/foo", updateResourceState);

        // verify the result
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo("bar");
        assertThat(result.getProperty("count")).isEqualTo(1L);

        // check in mongo directly
        assertThat(db.collectionExists("bar"));
        assertThat(db.collectionExists("foo")).isFalse();
        assertThat(db.getCollection("bar").count()).isEqualTo(1L);
        assertThat(db.getCollection("bar").findOne().get("hello")).isEqualTo("world");
    }

    @Test
    public void renameCollectionAlreadyExists() throws Exception {
        db.dropDatabase(); // TODO: create a new DB here instead of dropping the old one
        assertThat(db.getCollectionNames()).hasSize(0);

        // create a couple of collections
        db.createCollection("foo", new BasicDBObject());
        db.createCollection("bar", new BasicDBObject());

        // check that the collections are there (Note: there is an internal index collection, so one more than expected)
        assertThat(db.getCollectionNames()).hasSize(3);
        assertThat(db.collectionExists("foo"));
        assertThat(db.collectionExists("bar"));

        // set the update resource state to include a new id value
        // Note that this matches an already existing collection
        ResourceState updateResourceState = new DefaultResourceState("bar");

        try {
            client.update(new RequestContext.Builder().build(), "/testApp/" + BASEPATH + "/foo", updateResourceState);
            Fail.fail();
        } catch (ResourceAlreadyExistsException e) {
            //expected
        }
    }

    @Test
    public void updateProperty() throws Exception {
        db.dropDatabase(); // TODO: create a new DB here instead of dropping the old one
        assertThat(db.getCollectionNames()).hasSize(0);

        // create a collection
        DBCollection collection = db.createCollection("foo", new BasicDBObject());
        DBObject dbObject = new BasicDBObject("_id", "testObject");
        dbObject.put("hello", "world");
        collection.insert(dbObject);

        // check that the collection is there (Note: there is an internal index collection, so one more than expected)
        assertThat(db.getCollectionNames()).hasSize(2);
        assertThat(db.getCollection("foo").count() == 1);

        // set the update resource state to include the same id value
        ResourceState updateResourceState = new DefaultResourceState();
        updateResourceState.putProperty("count", 500L);

        try {
            client.update(new RequestContext.Builder().build(), "/testApp/" + BASEPATH + "/foo", updateResourceState);
            Fail.fail();
        } catch (NotAcceptableException e) {
            //expected
        }
    }
}
