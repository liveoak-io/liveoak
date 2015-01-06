/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.mongo;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import io.liveoak.common.codec.DefaultResourceState;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.exceptions.CreateNotSupportedException;
import io.liveoak.spi.exceptions.ResourceNotFoundException;
import io.liveoak.spi.state.ResourceState;
import org.bson.types.ObjectId;
import org.fest.assertions.Fail;
import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 */
public class MongoDBResourceUpdateTest extends BaseMongoDBTest {

    @Test
    public void simpleUpdate() throws Exception {
        String methodName = "testSimpleUpdate";
        assertThat(db.getCollection(methodName).getCount()).isEqualTo(0);

        // create the object using the mongo driver directly
        BasicDBObject object = new BasicDBObject();
        object.append("foo", "bar");
        db.getCollection(methodName).insert(object);
        assertEquals(1, db.getCollection(methodName).getCount());
        String id = "ObjectId(\"" + object.getObjectId("_id").toString() + "\")";

        // update the resource using the client.update method
        ResourceState resourceState = new DefaultResourceState();
        resourceState.putProperty("foo", "baz");

        ResourceState result = client.update(new RequestContext.Builder().build(), "/testApp/" + BASEPATH + "/" + methodName + "/" + id, resourceState);

        // verify the result
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(id);
        assertThat(result.getProperty("foo")).isEqualTo("baz");

        // verify db content
        assertThat(db.getCollection(methodName).getCount()).isEqualTo(1);
        DBObject dbObject = db.getCollection(methodName).findOne();
        assertEquals("baz", dbObject.get("foo"));
        assertEquals(new ObjectId(id.substring("ObjectId(\"".length(), id.length() - "\")".length())), dbObject.get("_id"));
    }

    @Test
    public void embeddedUpdate() throws Exception {
        String methodName = "testEmebeddedUpdate";
        assertThat(db.getCollection(methodName).getCount()).isEqualTo(0);

        //create the object using the mongo driver directly
        BasicDBObject object = new BasicDBObject();
        object.append("_id", "parent");
        object.append("test", 123);

        BasicDBObject child = new BasicDBObject();
        child.append("id", "foo");
        child.append("test", "123");

        BasicDBObject gChild = new BasicDBObject();
        gChild.append("id", "bar");
        gChild.append("testing", "one-two-three");

        child.append("bar", gChild);
        object.append("foo", child);

        db.getCollection(methodName).insert(object);
        assertEquals(1, db.getCollection(methodName).getCount());

        // update the resource using the client.update method
        ResourceState state = client.read(new RequestContext.Builder().build(), "/testApp/" + BASEPATH + "/" + methodName + "/parent");
        state.removeProperty("test");

        ResourceState fooState = state.getProperty("foo", true, ResourceState.class);
        fooState.putProperty("test", 123);

        ResourceState barState = fooState.getProperty("bar", true, ResourceState.class);
        barState.putProperty("id", "baz");
        barState.putProperty("testing", "1-2-3");

        ResourceState updatedState = client.update(new RequestContext.Builder().build(), "/testApp/" + BASEPATH + "/" + methodName + "/parent", state);

        assertThat(updatedState.id()).isEqualTo("parent");
        assertThat(updatedState.getPropertyNames().size()).isEqualTo(1);

        ResourceState updatedFoo = updatedState.getProperty("foo", true, ResourceState.class);
        assertThat(updatedFoo.id()).isNull();
        assertThat(updatedFoo.getProperty("id")).isEqualTo("foo");
        assertThat(updatedFoo.getProperty("test")).isEqualTo(123);

        ResourceState updatedBar = updatedFoo.getProperty("bar", true, ResourceState.class);
        assertThat(updatedBar.id()).isNull();
        assertThat(updatedBar.getProperty("id")).isEqualTo("baz");
        assertThat(updatedBar.getProperty("testing")).isEqualTo("1-2-3");

        // verify db content
        assertThat(db.getCollection(methodName).getCount()).isEqualTo(1);
        DBObject dbObject = db.getCollection(methodName).findOne();
        assertThat(dbObject.get("_id")).isEqualTo("parent");
        assertThat(dbObject.keySet().size()).isEqualTo(2);

        DBObject fooObject = (DBObject) dbObject.get("foo");
        assertThat(fooObject).isNotNull();
        assertThat(fooObject.keySet().size()).isEqualTo(3);
        assertThat(fooObject.get("_id")).isNull();
        assertThat(fooObject.get("id")).isEqualTo("foo");
        assertThat(fooObject.get("test")).isEqualTo(123);

        DBObject barObject = (DBObject) fooObject.get("bar");
        assertThat(barObject).isNotNull();
        assertThat(barObject.keySet().size()).isEqualTo(2);
        assertThat(barObject.get("_id")).isNull();
        assertThat(barObject.get("id")).isEqualTo("baz");
        assertThat(barObject.get("testing")).isEqualTo("1-2-3");
    }

    @Test
    public void childDirectUpdate() throws Exception {
        String methodName = "testChildDirectUpdate";
        assertThat(db.getCollection(methodName).getCount()).isEqualTo(0);

        // create the object using the mongo driver directly
        BasicDBObject object = new BasicDBObject();
        object.append("foo", new BasicDBObject("bar", "baz"));
        db.getCollection(methodName).insert(object);
        assertEquals(1, db.getCollection(methodName).getCount());
        String id = "ObjectId(\"" + object.getObjectId("_id").toString() + "\")";

        // update the resource using the client.update method
        ResourceState resourceState = new DefaultResourceState();
        resourceState.putProperty("bar", 123);

        // should not be able to directly update a child object
        try {
            client.update(new RequestContext.Builder().build(), "/testApp/" + BASEPATH + "/" + methodName + "/" + id + "/foo", resourceState);
            Fail.fail();
        } catch (CreateNotSupportedException e) {
            // expected
        }

        assertThat((DBObject) object).isEqualTo(db.getCollection(methodName).findOne());
    }

    @Test
    public void grandchildDirectUpdate() throws Exception {
        String methodName = "testGrandChildDirectUpdate";
        assertThat(db.getCollection(methodName).getCount()).isEqualTo(0);

        // create the object using the mongo driver directly
        BasicDBObject object = new BasicDBObject();
        object.append("foo", new BasicDBObject("bar", new BasicDBObject("baz", "ABC")));
        db.getCollection(methodName).insert(object);
        assertEquals(1, db.getCollection(methodName).getCount());
        String id = "ObjectId(\"" + object.getObjectId("_id").toString() + "\")";

        // update the resource using the client.update method
        ResourceState resourceState = new DefaultResourceState();
        resourceState.putProperty("baz", "XYZ");

        try {
            client.update(new RequestContext.Builder().build(), "/testApp/" + BASEPATH + "/" + methodName + "/" + id + "/foo/bar", resourceState);
            Fail.fail();
        } catch (ResourceNotFoundException e) {
            // expected
        }

        assertThat((DBObject) object).isEqualTo(db.getCollection(methodName).findOne());
    }
}
