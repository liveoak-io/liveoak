/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.mongo;

import java.util.Collection;
import java.util.Iterator;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import io.liveoak.common.DefaultReturnFields;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.exceptions.ResourceNotFoundException;
import io.liveoak.spi.state.ResourceState;
import org.bson.types.ObjectId;
import org.fest.assertions.Fail;
import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 */
public class MongoDBResourceReadTest extends BaseMongoDBTest {

    @Test
    public void getSimple() throws Exception {
        String methodName = "testSimpleGet";
        assertFalse(db.collectionExists(methodName));

        // create the object using the mongo driver directly
        BasicDBObject object = new BasicDBObject();
        object.append("foo", "bar");
        db.getCollection(methodName).insert(object);
        assertEquals(1, db.getCollection(methodName).getCount());
        String id = "ObjectId(\"" + object.getObjectId("_id").toString() + "\")";

        ResourceState result = client.read(new RequestContext.Builder().build(), "/testApp/" + BASEPATH + "/" + methodName + "/" + id);

        // verify response
        assertThat(result).isNotNull();
        assertThat(result.getProperty("foo")).isEqualTo("bar");
    }

    @Test
    public void similarIds() throws Exception {
        String methodName = "testSimilarIds";
        assertFalse(db.collectionExists(methodName));

        // create the object using the mongo driver directly
        BasicDBObject objectWithObjectID = new BasicDBObject("_id", new ObjectId("01234567890ABCDEF0123456"));
        objectWithObjectID.append("foo", "bar");
        db.getCollection(methodName).insert(objectWithObjectID);

        BasicDBObject objectWithStringID = new BasicDBObject("_id", "01234567890ABCDEF0123456");
        objectWithStringID.append("foo", "baz");
        db.getCollection(methodName).insert(objectWithStringID);

        assertEquals(2, db.getCollection(methodName).getCount());

        ResourceState resultObject = client.read(new RequestContext.Builder().build(), "/testApp/" + BASEPATH + "/" + methodName + "/ObjectId(\"01234567890ABCDEF0123456\")");
        ResourceState resultString = client.read(new RequestContext.Builder().build(), "/testApp/" + BASEPATH + "/" + methodName + "/01234567890ABCDEF0123456");

        assertThat(resultObject.getProperty("foo")).isEqualTo("bar");
        assertThat(resultString.getProperty("foo")).isEqualTo("baz");
    }

    @Test
    public void getWithEmbedded() throws Exception {
        String methodName = "testGetWithEmbedded";
        assertFalse(db.collectionExists(methodName));

        // create the object using the mongo driver directly
        BasicDBObject object = new BasicDBObject();
        object.append("foo", "bar");
        object.append("child", new BasicDBObject().append("ABC", "XYZ").append("id", "child"));
        db.getCollection(methodName).insert(object);
        assertEquals(1, db.getCollection(methodName).getCount());
        String id = "ObjectId(\"" + object.getObjectId("_id").toString() + "\")";

        ResourceState result = client.read(new RequestContext.Builder().build(), "/testApp/" + BASEPATH + "/" + methodName + "/" + id);

        // verify response
        assertThat(result).isNotNull();
        assertThat(result.getProperty("foo")).isEqualTo("bar");
        ResourceState resultChild = (ResourceState) result.getProperty("child");
        assertThat(resultChild.getProperty("ABC")).isEqualTo("XYZ");
        assertThat(resultChild.getProperty("id")).isEqualTo("child");
        assertThat(resultChild.getProperty("_id")).isNull();
    }

    @Test
    public void getEmbeddedWithReturnFields() throws Exception {
        String methodName = "testGetEmbeddedWithReturnFields";
        assertFalse(db.collectionExists(methodName));

        // create the object using the mongo driver directly
        BasicDBObject object = new BasicDBObject();
        object.append("foo", "bar");
        object.append("child", new BasicDBObject().append("ABC", "XYZ").append("test", "123").append("grandchild", new BasicDBObject("123", 456)));
        object.append("baz", "123");

        db.getCollection(methodName).insert(object);
        assertEquals(1, db.getCollection(methodName).getCount());
        String id = "ObjectId(\"" + object.getObjectId("_id").toString() + "\")";

        // check that we don't get back baz, but that we do get the child and grandchild
        RequestContext rCtx = new RequestContext.Builder().returnFields(new DefaultReturnFields("foo,child")).build();
        ResourceState result = client.read(rCtx, "/testApp/" + BASEPATH + "/" + methodName + "/" + id);

        // verify response
        assertThat(result).isNotNull();
        assertThat(result.getProperty("foo")).isEqualTo("bar");
        assertThat(result.getProperty("child")).isNotNull();
        assertThat(result.getProperty("baz")).isNull();
        ResourceState resultChild = (ResourceState) result.getProperty("child");
        assertThat(resultChild.getProperty("ABC")).isEqualTo("XYZ");
        assertThat(resultChild.getProperty("test")).isEqualTo("123");
        ResourceState resultGrandChild = (ResourceState) resultChild.getProperty("grandchild");
        assertThat(resultGrandChild.getProperty("123")).isEqualTo(456);

        // check that we don't get back the non specified embedded objects
        rCtx = new RequestContext.Builder().returnFields(new DefaultReturnFields("foo")).build();
        result = client.read(rCtx, "/testApp/" + BASEPATH + "/" + methodName + "/" + id);

        // verify response
        assertThat(result).isNotNull();
        assertThat(result.getProperty("foo")).isEqualTo("bar");
        assertThat(result.getProperty("child")).isNull();
        assertThat(result.getProperty("baz")).isNull();

        // check that we don't get back the non specified embedded child objects
        rCtx = new RequestContext.Builder().returnFields(new DefaultReturnFields("foo,child(ABC,test)")).build();
        result = client.read(rCtx, "/testApp/" + BASEPATH + "/" + methodName + "/" + id);

        // verify response
        assertThat(result).isNotNull();
        assertThat(result.getProperty("foo")).isEqualTo("bar");
        assertThat(result.getProperty("child")).isNotNull();
        assertThat(result.getProperty("baz")).isNull();
        resultChild = (ResourceState) result.getProperty("child");
        assertThat(resultChild.getProperty("ABC")).isEqualTo("XYZ");
        assertThat(resultChild.getProperty("test")).isEqualTo("123");
        assertThat(resultChild.getProperty("grandchild")).isNull();
    }

    @Test
    public void getChildDirectly() throws Exception {
        String methodName = "testGetChildDirectly";
        assertThat(db.collectionExists(methodName)).isFalse();

        // create the object using the mongo driver directly
        BasicDBObject object = new BasicDBObject();
        object.append("foo", "bar").append("abc", "123").append("obj", new BasicDBObject("foo2", "bar2"));
        object.append("child", new BasicDBObject("grandchild", new BasicDBObject("foo3", "bar3")));
        db.getCollection(methodName).insert(object);
        assertEquals(1, db.getCollection(methodName).getCount());
        String id = "ObjectId(\"" + object.getObjectId("_id").toString() + "\")";

        try {
            client.read(new RequestContext.Builder().build(), "/testApp/" + BASEPATH + "/" + methodName + "/" + id + "/child");
            Fail.fail();
        } catch (ResourceNotFoundException e) {
            // expected
        }
    }

    @Test
    public void getArray() throws Exception {
        String methodName = "testGetArray";
        assertThat(db.collectionExists(methodName)).isFalse();

        // create the object using the mongo driver directly
        BasicDBObject object = new BasicDBObject("_id", "foobaz");

        BasicDBList list = new BasicDBList();
        list.add(1);
        list.add("A");
        list.add(new BasicDBObject("foo", "bar"));
        object.append("array", list);

        object.append("eArray", new BasicDBList());

        BasicDBList list2 = new BasicDBList();
        list2.add("XYZ");
        list2.add(new BasicDBObject("hello", "world"));
        list2.add(2);

        object.append("child", new BasicDBObject("array", list2));
        db.getCollection(methodName).insert(object);
        assertEquals(1, db.getCollection(methodName).getCount());

        ResourceState result = client.read(new RequestContext.Builder().returnFields(new DefaultReturnFields("*(*(*))")).build(), "/testApp/" + BASEPATH + "/" + methodName + "/foobaz");

        // verify the result
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo("foobaz");
        // check the array
        assertThat(((Collection) result.getProperty("array")).size()).isEqualTo(3);
        Iterator arrayIter = ((Collection) result.getProperty("array")).iterator();
        assertThat(arrayIter.next()).isEqualTo(1);
        assertThat(arrayIter.next()).isEqualTo("A");

        ResourceState child = (ResourceState) arrayIter.next();
        assertThat(child.getProperty("foo")).isEqualTo("bar");

        assertThat(arrayIter.hasNext()).isFalse();
        assertThat(((Collection) result.getProperty("eArray"))).isEmpty();

        try {
            client.read(new RequestContext.Builder().returnFields(new DefaultReturnFields("*(*(*))")).build(), "/testApp/" + BASEPATH + "/" + methodName + "/foobaz/child");
            Fail.fail();
        } catch (ResourceNotFoundException e) {
            // expected
        }
    }

    @Test
    public void getCollection() throws Exception {
        String methodName = "testGetCollection";
        assertThat(db.collectionExists(methodName)).isFalse();

        // create the object using the mongo driver directly
        BasicDBObject object = new BasicDBObject("_id", "foobaz");

        BasicDBList list = new BasicDBList();
        list.add(1);
        list.add("A");
        list.add(new BasicDBObject("_id", "test123").append("foo", "bar"));
        object.append("array", list);

        db.getCollection(methodName).insert(object);
        assertEquals(1, db.getCollection(methodName).getCount());

        try {
            client.read(new RequestContext.Builder().build(), "/testApp/" + BASEPATH + "/" + methodName + "/foobaz/array");
            Fail.fail();
        } catch (ResourceNotFoundException e) {
            // expected
        }
    }

    @Test
    public void getInvalidId() throws Exception {
        String methodName = "testGetInvalidId";
        assertFalse(db.collectionExists(methodName));

        try {
            client.read(new RequestContext.Builder().build(), "/testApp/" + BASEPATH + "/" + methodName + "/foobar123");
            fail("shouldn't get here");
        } catch (ResourceNotFoundException rnfe) {
            // expected
        }
    }

    @Test
    public void getNonExistentId() throws Exception {
        String methodName = "testGetNonExistantId";
        assertFalse(db.collectionExists(methodName));

        ObjectId objectId = new ObjectId();

        try {
            client.read(new RequestContext.Builder().build(), "/testApp/" + BASEPATH + "/" + methodName + "/" + objectId);
            fail("shouldn't get here");
        } catch (ResourceNotFoundException rnfe) {
            // expected
        }
    }
}
