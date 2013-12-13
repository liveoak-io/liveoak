/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */

package io.liveoak.mongo;

import static org.fest.assertions.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

import org.bson.types.ObjectId;
import org.fest.assertions.Fail;
import org.junit.Test;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

import io.liveoak.common.codec.DefaultResourceState;
import io.liveoak.spi.CreateNotSupportedException;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.ResourceNotFoundException;
import io.liveoak.spi.state.ResourceState;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 */
public class MongoDBResourceUpdateTest extends BaseMongoDBTest {

    @Test
    public void testSimpleUpdate() throws Exception {
        String methodName = "testSimpleUpdate";
        assertThat(db.getCollection(methodName).getCount()).isEqualTo(0);

        // create the object using the mongo driver directly
        BasicDBObject object = new BasicDBObject();
        object.append("foo", "bar");
        db.getCollection(methodName).insert(object);
        assertEquals(1, db.getCollection(methodName).getCount());
        String id = "_mOI:" + object.getObjectId("_id").toString();

        // update the resource using the client.update method
        ResourceState resourceState = new DefaultResourceState();
        resourceState.putProperty("foo", "baz");

        ResourceState result = client.update(new RequestContext.Builder().build(), BASEPATH + "/" + methodName + "/" + id, resourceState);

        // verify the result
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(id);
        assertThat(result.getProperty("foo")).isEqualTo("baz");

        // verify db content
        assertThat(db.getCollection(methodName).getCount()).isEqualTo(1);
        DBObject dbObject = db.getCollection(methodName).findOne();
        assertEquals("baz", dbObject.get("foo"));
        assertEquals(new ObjectId(id.substring("_mOI:".length())), dbObject.get("_id"));
    }

    @Test
    public void testChildDirectUpdate() throws Exception {
        String methodName = "testChildDirectUpdate";
        assertThat(db.getCollection(methodName).getCount()).isEqualTo(0);

        // create the object using the mongo driver directly
        BasicDBObject object = new BasicDBObject();
        object.append("foo", new BasicDBObject("bar", "baz"));
        db.getCollection(methodName).insert(object);
        assertEquals(1, db.getCollection(methodName).getCount());
        String id = "_mOI:" + object.getObjectId("_id").toString();

        // update the resource using the client.update method
        ResourceState resourceState = new DefaultResourceState();
        resourceState.putProperty("bar", 123);

        // should not be able to directly update a child object
        try {
            ResourceState result = client.update(new RequestContext.Builder().build(), BASEPATH + "/" + methodName + "/" + id + "/foo", resourceState);
            Fail.fail();
        } catch (CreateNotSupportedException e) {
            // expected
        }

        assertThat((DBObject) object).isEqualTo(db.getCollection(methodName).findOne());
    }

    @Test
    public void testGrandChildDirectUpdate() throws Exception {
        String methodName = "testGrandChildDirectUpdate";
        assertThat(db.getCollection(methodName).getCount()).isEqualTo(0);

        // create the object using the mongo driver directly
        BasicDBObject object = new BasicDBObject();
        object.append("foo", new BasicDBObject("bar", new BasicDBObject("baz", "ABC")));
        db.getCollection(methodName).insert(object);
        assertEquals(1, db.getCollection(methodName).getCount());
        String id = "_mOI:" + object.getObjectId("_id").toString();

        // update the resource using the client.update method
        ResourceState resourceState = new DefaultResourceState();
        resourceState.putProperty("baz", "XYZ");

        try {
            ResourceState result = client.update(new RequestContext.Builder().build(), BASEPATH + "/" + methodName + "/" + id + "/foo/bar", resourceState);
            Fail.fail();
        } catch (ResourceNotFoundException e) {
            // expected
        }

        assertThat((DBObject) object).isEqualTo(db.getCollection(methodName).findOne());
    }
}
