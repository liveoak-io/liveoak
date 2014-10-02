/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */

package io.liveoak.mongo;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import io.liveoak.common.DefaultReturnFields;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.exceptions.ResourceNotFoundException;
import io.liveoak.spi.state.ResourceState;
import org.bson.types.ObjectId;
import org.fest.assertions.Fail;
import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 */
public class MongoDBResourceDeleteTest extends BaseMongoDBTest {

    @Test
    public void testSimpleDelete() throws Exception {
        String methodName = "testSimpleDelete";
        assertThat(db.getCollectionNames().contains(methodName)).isFalse();

        // create the object using the mongo driver directly
        BasicDBObject object = new BasicDBObject();
        object.append("foo", "bar");
        db.getCollection(methodName).insert(object);
        assertThat(db.getCollection(methodName).getCount()).isEqualTo(1);
        String id = "ObjectId(\"" + object.getObjectId("_id").toString() + "\")";

        // now delete the object
        ResourceState result = client.delete(new RequestContext.Builder().build(), "/testApp/" + BASEPATH + "/" + methodName + "/" + id);

        // verify we are getting back the object which was deleted
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(id);
        assertThat(result.getProperty("foo")).isEqualTo("bar");

        // check that it got deleted in the db
        assertThat(db.getCollection(methodName).getCount()).isEqualTo(0);
    }

    @Test
    public void testDirectDeleteProperty() throws Exception {
        String methodName = "testDirectDeleteProperty";
        assertThat(db.getCollectionNames().contains(methodName)).isFalse();

        // create the object using the mongo driver directly
        BasicDBObject object = new BasicDBObject();
        object.append("foo", new BasicDBObject("bar", "123"));
        db.getCollection(methodName).insert(object);
        String id = object.getObjectId("_id").toString();
        assertThat(db.getCollection(methodName).findOne(new BasicDBObject("_id", new ObjectId(id)))).isNotNull();

        // we should not be able to directly delete a child property
        try {
            ResourceState result = client.delete(new RequestContext.Builder().build(), "/testApp/" + BASEPATH + "/" + methodName + "/" + id + "/foo");
            Fail.fail("We should not be able to directly delete a property");
        } catch (ResourceNotFoundException e) {
            // expected
        }

        assertThat((DBObject) object).isEqualTo(db.getCollection(methodName).findOne());
    }

    @Test
    public void testDeleteChildObject() throws Exception {
        String methodName = "testDeleteChildObject";
        assertThat(db.getCollectionNames().contains(methodName)).isFalse();

        // create the object using the mongo driver directly
        BasicDBObject object = new BasicDBObject();
        object.append("foo", new BasicDBObject("bar", new BasicDBObject("ABC", 123)));
        db.getCollection(methodName).insert(object);
        String id = object.getObjectId("_id").toString();
        assertThat(db.getCollection(methodName).findOne(new BasicDBObject("_id", new ObjectId(id)))).isNotNull();

        // we should not be able to directly delete a child object
        try {
            ResourceState result = client.delete(new RequestContext.Builder().returnFields(new DefaultReturnFields("bar(ABC(*))")).build(), "/testApp/" + BASEPATH + "/" + methodName
                    + "/" + id + "/foo");
            Fail.fail("We should not be able to directly delete a child object");
        } catch (ResourceNotFoundException e) {
            // expected
        }

        assertThat((DBObject) object).isEqualTo(db.getCollection(methodName).findOne());
    }

    @Test
    public void testDeleteChildProperty() throws Exception {
        String methodName = "testDeleteChildProperty";
        assertThat(db.getCollectionNames().contains(methodName)).isFalse();

        // create the object using the mongo driver directly
        BasicDBObject object = new BasicDBObject();
        object.append("foo", new BasicDBObject("bar", new BasicDBObject("ABC", 123)));
        db.getCollection(methodName).insert(object);
        String id = object.getObjectId("_id").toString();
        assertThat(db.getCollection(methodName).findOne(new BasicDBObject("_id", new ObjectId(id)))).isNotNull();

        // we should not be able to directly delete a child property
        try {
            ResourceState result = client.delete(new RequestContext.Builder().build(), "/testApp/" + BASEPATH + "/" + methodName + "/" + id + "/foo/bar");
            Fail.fail("We should not be able to directly delete a property on a child object");
        } catch (ResourceNotFoundException e) {
            // expected
        }

        assertThat((DBObject) object).isEqualTo(db.getCollection(methodName).findOne());
    }

    @Test
    public void testDeleteGrandchildObject() throws Exception {
        String methodName = "testDeleteGrandchildObject";
        assertThat(db.getCollectionNames().contains(methodName)).isFalse();

        // create the object using the mongo driver directly
        BasicDBObject object = new BasicDBObject();
        object.append("foo", new BasicDBObject("bar", new BasicDBObject("ABC", new BasicDBObject("123", "XYZ"))));
        db.getCollection(methodName).insert(object);
        String id = object.getObjectId("_id").toString();
        assertThat(db.getCollection(methodName).findOne(new BasicDBObject("_id", new ObjectId(id)))).isNotNull();

        // we should not be able to directly delete a child object
        try {
            ResourceState result = client.delete(new RequestContext.Builder().returnFields(new DefaultReturnFields("bar(ABC(*))")).build(), "/testApp/" + BASEPATH + "/" + methodName
                    + "/" + id + "/foo");
            Fail.fail();
        } catch (ResourceNotFoundException e) {
            // expected
        }

        assertThat((DBObject) object).isEqualTo(db.getCollection(methodName).findOne());
    }

    @Test
    public void testDeleteNonExistantCollection() throws Exception {
        String methodName = "testDeleteNonExistantCollection";
        assertThat(db.getCollectionNames().contains(methodName)).isFalse();

        try {
            ResourceState result = client.delete(new RequestContext.Builder().build(), "/testApp/" + BASEPATH + "/" + methodName);
            Fail.fail("shouldn't get here");
        } catch (ResourceNotFoundException rnfe) {
            // expected
        }
    }

    @Test
    public void testDeleteCollection() throws Exception {
        String methodName = "testDeleteCollection";
        assertThat(db.getCollectionNames().contains(methodName)).isFalse();

        // create the collection
        db.createCollection(methodName, new BasicDBObject());
        assertThat(db.getCollectionNames().contains(methodName)).isTrue();

        ResourceState result = client.delete(new RequestContext.Builder().build(), "/testApp/" + BASEPATH + "/" + methodName);

        // check that it was actually deleted
        assertThat(db.getCollectionNames().contains(methodName)).isFalse();
    }

    @Test
    public void testDeleteInvalidId() throws Exception {
        String methodName = "testDeleteInvalidId";
        assertThat(db.getCollectionNames().contains(methodName)).isFalse();

        try {
            ResourceState result = client.delete(new RequestContext.Builder().build(), "/testApp/" + BASEPATH + "/foobar123");
            Fail.fail("shouldn't get here");
        } catch (ResourceNotFoundException e) {
            // expected
        }
    }

    @Test
    public void testDeleteNonExistantId() throws Exception {
        String methodName = "testDeleteNonExistantId";
        assertThat(db.getCollectionNames().contains(methodName)).isFalse();

        ObjectId id = new ObjectId();

        try {
            ResourceState result = client.delete(new RequestContext.Builder().build(), "/testApp/" + BASEPATH + "/" + methodName + "/" + id.toString());
            Fail.fail("shouldn't get here");
        } catch (ResourceNotFoundException e) {
            // expected
        }

    }

}
