/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.mongo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import io.liveoak.common.codec.DefaultResourceState;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.exceptions.ResourceAlreadyExistsException;
import io.liveoak.spi.state.ResourceState;
import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 */
public class MongoDBResourceCreateTest extends BaseMongoDBTest {

    @Test
    public void simpleCreate() throws Exception {
        String methodName = "testSimpleCreate";
        assertFalse(db.collectionExists(methodName));
        db.createCollection(methodName, new BasicDBObject());

        ResourceState state = new DefaultResourceState();
        state.putProperty("foo", "bar");
        ResourceState result = client.create(new RequestContext.Builder().build(), "/testApp/" + BASEPATH + "/" + methodName, state);

        // verify response
        assertThat(result).isNotNull();
        assertThat(result.id()).isNotNull();
        assertThat(result.getProperty("foo")).isEqualTo("bar");

        // verify what is stored in the mongo db
        assertThat(db.collectionExists(methodName)).isTrue();
        assertThat(db.getCollection(methodName).getCount()).isEqualTo(1);

        DBObject dbObject = db.getCollection(methodName).findOne();
        assertThat(dbObject.get("foo")).isEqualTo("bar");
    }

    @Test
    public void simpleCreateWithId() throws Exception {
        String methodName = "testSimpleCreateWithID";
        assertFalse(db.collectionExists(methodName));
        db.createCollection(methodName, new BasicDBObject());

        ResourceState state = new DefaultResourceState("helloworld");
        state.putProperty("foo", "bar");
        ResourceState result = client.create(new RequestContext.Builder().build(), "/testApp/" + BASEPATH + "/" + methodName, state);

        // verify response
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo("helloworld");
        assertThat(result.getProperty("foo")).isEqualTo("bar");

        // verify what is stored in the mongo db
        assertTrue(db.collectionExists(methodName));
        assertEquals(1, db.getCollection(methodName).getCount());
        DBObject dbObject = db.getCollection(methodName).findOne(new BasicDBObject("_id", "helloworld"));
        assertEquals("bar", dbObject.get("foo"));
    }

    @Test
    public void createAlreadyExists() throws Exception {
        String methodName = "testCreateAlreadyExist";
        assertFalse(db.collectionExists(methodName));
        db.createCollection(methodName, new BasicDBObject());
        db.getCollection(methodName).insert(new BasicDBObject("_id", "helloworld"));

        ResourceState state = new DefaultResourceState("helloworld");
        state.putProperty("foo", "bar");

        try {
            client.create(new RequestContext.Builder().build(), "/testApp/" + BASEPATH + "/" + methodName, state);
            fail();
        } catch (ResourceAlreadyExistsException e) {
            // expected
        }
    }

    @Test
    public void createWithArrays() throws Exception {
        String methodName = "testCreateWithArrays";
        assertFalse(db.collectionExists(methodName));
        db.createCollection(methodName, new BasicDBObject());

        ResourceState state = new DefaultResourceState("testArrays");

        ArrayList arr = new ArrayList();
        arr.add(1);

        ResourceState obj1 = new DefaultResourceState();
        obj1.putProperty("foo", "bar");
        obj1.putProperty("test", 321);
        arr.add(obj1);

        arr.add("ABC");

        ResourceState obj2 = new DefaultResourceState();
        obj2.putProperty("123", 456);
        obj2.putProperty("hello", "world");
        ResourceState childObj = new DefaultResourceState();
        childObj.putProperty("foo", "baz");
        obj2.putProperty("child", childObj);
        arr.add(obj2);

        arr.add(123);

        state.putProperty("arr", arr);

        RequestContext requestContext = new RequestContext.Builder().build();// .returnFields(new ReturnFieldsImpl("*(arr(*(*)))")).build();
        ResourceState result = client.create(requestContext, "/testApp/" + BASEPATH + "/" + methodName, state);

        // verify the result
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo("testArrays");
        assertThat(result.getProperty("arr")).isNotNull();

        List list = (List) result.getProperty("arr");
        assertThat(list.get(0)).isEqualTo(1);

        ResourceState childResourceState = (ResourceState) list.get(1);
        assertThat(childResourceState.getProperty("foo")).isEqualTo("bar");
        assertThat(childResourceState.getProperty("test")).isEqualTo(321);

        assertThat(list.get(2)).isEqualTo("ABC");

        ResourceState childResourceState2 = (ResourceState) list.get(3);
        assertThat(childResourceState2.getProperty("123")).isEqualTo(456);
        assertThat(childResourceState2.getProperty("hello")).isEqualTo("world");
        ResourceState grandchildResourceState = (ResourceState) childResourceState2.getProperty("child");
        assertThat(grandchildResourceState.getProperty("foo")).isEqualTo("baz");

        // verify what is in the database
        DBObject dbObject = db.getCollection(methodName).findOne();
        assertThat(dbObject).isNotNull();
        assertThat(dbObject.keySet().size()).isEqualTo(2);
        assertThat(dbObject.get("_id")).isEqualTo("testArrays");
        assertThat(dbObject.get("arr")).isNotNull();

        BasicDBList dbList = (BasicDBList) dbObject.get("arr");
        assertThat(dbList.get(0)).isEqualTo(1);

        BasicDBObject childDBObject = (BasicDBObject) dbList.get(1);
        assertThat(childDBObject.get("foo")).isEqualTo("bar");
        assertThat(childDBObject.get("test")).isEqualTo(321);

        assertThat(dbList.get(2)).isEqualTo("ABC");

        BasicDBObject childDBObject2 = (BasicDBObject) dbList.get(3);
        assertThat(childDBObject2.get("123")).isEqualTo(456);
        assertThat(childDBObject2.get("hello")).isEqualTo("world");
        BasicDBObject grandChildDBObject = (BasicDBObject) childDBObject2.get("child");
        assertThat(grandChildDBObject.get("foo")).isEqualTo("baz");
    }

    @Test
    public void createWithNestedArrays() throws Exception {
        String methodName = "testCreateWithNestedArrays";
        assertFalse(db.collectionExists(methodName));
        db.createCollection(methodName, new BasicDBObject());

        ResourceState state = new DefaultResourceState("testArraysNested");

        ArrayList arr = new ArrayList();
        arr.add(1.123);

        ArrayList nest1 = new ArrayList();
        nest1.add(2);
        nest1.add("XYZ");

        ArrayList nest2 = new ArrayList();
        nest2.add(3);
        ResourceState nestedObject = new DefaultResourceState();
        nestedObject.putProperty("foo", "bar");
        nest2.add(nestedObject);
        nest1.add(nest2);

        arr.add(nest1);
        arr.add("ABC");

        state.putProperty("arr", arr);

        RequestContext requestContext = new RequestContext.Builder().build();
        ResourceState result = client.create(requestContext, "/testApp/" + BASEPATH + "/" + methodName, state);

        // verify the result
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo("testArraysNested");
        assertThat(result.getProperty("arr")).isNotNull();

        List list = (List) result.getProperty("arr");
        assertThat(list.get(0)).isEqualTo(1.123);

        List childList = (List) list.get(1);
        assertThat(childList.get(0)).isEqualTo(2);
        assertThat(childList.get(1)).isEqualTo("XYZ");

        List grandchildList = (List) childList.get(2);
        assertThat(grandchildList.get(0)).isEqualTo(3);
        ResourceState nestedObjectRS = (ResourceState) grandchildList.get(1);
        assertThat(nestedObjectRS.getProperty("foo")).isEqualTo("bar");

        assertThat(list.get(2)).isEqualTo("ABC");

        // verify what is in the database
        DBObject dbObject = db.getCollection(methodName).findOne();
        assertThat(dbObject).isNotNull();
        assertThat(dbObject.keySet().size()).isEqualTo(2);
        assertThat(dbObject.get("_id")).isEqualTo("testArraysNested");
        assertThat(dbObject.get("arr")).isNotNull();

        BasicDBList dbList = (BasicDBList) dbObject.get("arr");
        assertThat(dbList.get(0)).isEqualTo(1.123);

        BasicDBList nestedDBList = (BasicDBList) dbList.get(1);
        assertThat(nestedDBList.get(0)).isEqualTo(2);
        assertThat(nestedDBList.get(1)).isEqualTo("XYZ");

        BasicDBList nestedDBList2 = (BasicDBList) nestedDBList.get(2);
        assertThat(nestedDBList2.get(0)).isEqualTo(3);
        BasicDBObject nestedDBObject = (BasicDBObject) nestedDBList2.get(1);
        assertThat(nestedDBObject.get("foo")).isEqualTo("bar");

        assertThat(dbList.get(2)).isEqualTo("ABC");
    }

    @Test
    public void complexCreate() throws Exception {
        String methodName = "testComplexCreate";
        assertFalse(db.collectionExists(methodName));
        db.createCollection(methodName, new BasicDBObject());

        ResourceState state = new DefaultResourceState("helloworld");
        state.putProperty("foo", "bar");
        state.putProperty("test", 123);
        Object[] arr2 = {1, 1, 2, 3, 5, 8, 13, 21};
        state.putProperty("arr", Arrays.asList(arr2));
        state.putProperty("arr2", arr2);

        ResourceState arrayObj = new DefaultResourceState();
        arrayObj.putProperty("array", "object");
        ArrayList arrayList = new ArrayList(Arrays.asList(arr2));
        arrayList.add(arrayObj);
        state.putProperty("arr3", arrayList);

        ResourceState obj = new DefaultResourceState();
        obj.putProperty("foo2", "bar2");
        obj.putProperty("test2", 321);

        ResourceState subObj = new DefaultResourceState();
        subObj.putProperty("abc", "xyz");

        obj.putProperty("subobject", subObj);
        state.putProperty("obj", obj);

        ResourceState result = client.create(new RequestContext.Builder().build(), "/testApp/" + BASEPATH + "/" + methodName, state);

        // verify the result
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo("helloworld");
        assertThat(result.getProperty("foo")).isEqualTo("bar");
        assertThat(result.getProperty("test")).isEqualTo(123);
        assertThat(result.getProperty("arr")).isNotNull();
        assertThat(result.getProperty("arr2")).isNotNull();
        // check the array
        assertThat(result.getProperty("arr").toString()).isEqualTo("[1, 1, 2, 3, 5, 8, 13, 21]");
        assertThat(result.getProperty("arr2").toString()).isEqualTo("[1, 1, 2, 3, 5, 8, 13, 21]");
        // check the child object

        // verify what is in the database
        DBObject dbObject = db.getCollection(methodName).findOne();
        assertThat(dbObject).isNotNull();
        assertThat(dbObject.keySet().size()).isEqualTo(7);
        assertThat(dbObject.get("_id")).isEqualTo("helloworld");
        assertThat(dbObject.get("foo")).isEqualTo("bar");
        assertThat(dbObject.get("test")).isEqualTo(123);
        assertThat(dbObject.get("arr").toString()).isEqualTo("[ 1 , 1 , 2 , 3 , 5 , 8 , 13 , 21]");
        assertThat(dbObject.get("arr2").toString()).isEqualTo("[ 1 , 1 , 2 , 3 , 5 , 8 , 13 , 21]");

        DBObject childDBObject1 = (DBObject) dbObject.get("obj");
        assertThat(childDBObject1).isNotNull();
        assertThat(childDBObject1.keySet().size()).isEqualTo(3);
        assertThat(childDBObject1.get("_id")).isNull();
        assertThat(childDBObject1.get("foo2")).isEqualTo("bar2");
        assertThat(childDBObject1.get("test2")).isEqualTo(321);

        DBObject grandchildDBObject = (DBObject) childDBObject1.get("subobject");
        assertThat(grandchildDBObject).isNotNull();
        assertThat(grandchildDBObject.keySet().size()).isEqualTo(1);
        assertThat(grandchildDBObject.get("_id")).isNull();
        assertThat(grandchildDBObject.get("abc")).isEqualTo("xyz");
    }

    @Test
    public void createWithNestedID() throws Exception {
        String methodName = "testCreateWithNestedID";
        assertFalse(db.collectionExists(methodName));
        db.createCollection(methodName, new BasicDBObject());

        ResourceState state = new DefaultResourceState("testIDNested");

        ResourceState nestedState = new DefaultResourceState();
        nestedState.putProperty("id", "foo");
        nestedState.putProperty("test", 123);

        ResourceState nestedNestedState =new DefaultResourceState();
        nestedNestedState.putProperty("id", "bar");
        nestedNestedState.putProperty("testing", "one-two-three");

        nestedState.putProperty("bar", nestedNestedState);

        state.putProperty("foo", nestedState);

        RequestContext requestContext = new RequestContext.Builder().build();
        ResourceState result = client.create(requestContext, "/testApp/" + BASEPATH + "/" + methodName, state);

        // verify the result
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo("testIDNested");
        assertThat(result.getProperty("foo")).isNotNull();

        ResourceState fooResult = result.getProperty("foo", true, ResourceState.class);
        assertThat(fooResult.id()).isNull();
        assertThat(fooResult.getProperty("id")).isEqualTo("foo");
        assertThat(fooResult.getProperty("test")).isEqualTo(123);
        assertThat(fooResult.getProperty("bar")).isNotNull();

        ResourceState barResult = fooResult.getProperty("bar", true, ResourceState.class);
        assertThat(barResult.id()).isNull();
        assertThat(barResult.getProperty("id")).isEqualTo("bar");
        assertThat(barResult.getProperty("testing")).isEqualTo("one-two-three");

        // verify what is in the database
        DBObject dbObject = db.getCollection(methodName).findOne();
        assertThat(dbObject).isNotNull();
        assertThat(dbObject.keySet().size()).isEqualTo(2);
        assertThat(dbObject.get("_id")).isEqualTo("testIDNested");
        assertThat(dbObject.get("foo")).isNotNull();

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
        assertThat(barObject.get("id")).isEqualTo("bar");
        assertThat(barObject.get("testing")).isEqualTo("one-two-three");
    }


}
