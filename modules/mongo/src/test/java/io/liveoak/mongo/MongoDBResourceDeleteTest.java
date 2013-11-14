/*
 * Copyright 2013 Red Hat, Inc.
 *
 * Red Hat licenses this file to you under the Apache License, version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License.  You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package io.liveoak.mongo;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import io.liveoak.container.ReturnFieldsImpl;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.ResourceNotFoundException;
import io.liveoak.spi.state.ResourceState;
import org.bson.types.ObjectId;
import org.fest.assertions.Fail;
import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;
import org.fest.assertions.Fail;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 */
public class MongoDBResourceDeleteTest extends NewBaseMongoDBTest{

    @Test
    public void testSimpleDelete() throws Exception {
        String methodName = "testSimpleDelete";
        assertThat(db.getCollectionNames().contains(methodName)).isFalse();

        // create the object using the mongo driver directly
        BasicDBObject object = new BasicDBObject();
        object.append("foo", "bar");
        db.getCollection(methodName).insert(object);
        assertThat(db.getCollection(methodName).getCount()).isEqualTo(1);
        String id = object.getObjectId("_id").toString();

        // now delete the object
        ResourceState result = connector.delete(new RequestContext.Builder().build(), BASEPATH + "/" + methodName + "/" + id);

        // verify we are getting back the object which was deleted
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(id);
        assertThat(result.getProperty("foo")).isEqualTo("bar");

        // check that it got deleted in the db
        assertThat(db.getCollection(methodName).getCount()).isEqualTo(0);
    }

    @Test
    public void testDeleteChildProperty() throws Exception {
        String methodName = "testDeleteChildProperty";
        assertThat(db.getCollectionNames().contains(methodName)).isFalse();

        // create the object using the mongo driver directly
        BasicDBObject object = new BasicDBObject();
        object.append("foo", new BasicDBObject("bar", "123"));
        db.getCollection(methodName).insert(object);
        String id = object.getObjectId("_id").toString();
        assertThat(db.getCollection(methodName).findOne(new BasicDBObject("_id", new ObjectId(id)))).isNotNull();

        // now delete the object
        ResourceState result = connector.delete(new RequestContext.Builder().build(), BASEPATH + "/" + methodName + "/" + id + "/foo");

        // verify we are getting back the object which was deleted
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo("foo");
        assertThat(result.getProperty("bar")).isEqualTo("123");

        // check that it got deleted in the db
        DBObject dbObject = db.getCollection(methodName).findOne(new BasicDBObject("_id", new ObjectId(id)));
        assertThat(dbObject).isNotNull();
        assertThat(dbObject.get("foo")).isNull();
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

        // now delete the object
        ResourceState result = connector.delete(new RequestContext.Builder().returnFields( new ReturnFieldsImpl( "bar(ABC(*))")).build(), BASEPATH + "/" + methodName + "/" + id + "/foo");

        // verify we are getting back the object which was deleted
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo("foo");
        ResourceState childResourceState = (ResourceState) result.getProperty("bar");
        assertThat(childResourceState.getProperty("ABC")).isEqualTo(123);

        // check that it got deleted in the db
        DBObject dbObject = db.getCollection(methodName).findOne(new BasicDBObject("_id", new ObjectId(id)));
        assertThat(dbObject).isNotNull();
        assertThat(dbObject.get("foo")).isNull();
    }

    @Test
    public void testDeleteGrandchildProperty() throws Exception {
        String methodName = "testDeleteGrandchildProperty";
        assertThat(db.getCollectionNames().contains(methodName)).isFalse();

        // create the object using the mongo driver directly
        BasicDBObject object = new BasicDBObject();
        object.append("foo", new BasicDBObject("bar", new BasicDBObject("ABC", 123)));
        db.getCollection(methodName).insert(object);
        String id = object.getObjectId("_id").toString();
        assertThat(db.getCollection(methodName).findOne(new BasicDBObject("_id", new ObjectId(id)))).isNotNull();

        // now delete the object
        ResourceState result = connector.delete(new RequestContext.Builder().build(), BASEPATH + "/" + methodName + "/" + id + "/foo/bar");

        // verify we are getting back the object which was deleted
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo("bar");
        assertThat(result.getProperty("ABC")).isEqualTo(123);

        // check that it got deleted in the db
        DBObject dbObject = db.getCollection(methodName).findOne(new BasicDBObject("_id", new ObjectId(id)));
        assertThat(dbObject).isNotNull();
        DBObject childDBObject = (DBObject) dbObject.get("foo");
        assertThat(childDBObject).isNotNull();
        assertThat(childDBObject.get("bar")).isNull();
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

        // now delete the object
        ResourceState result = connector.delete(new RequestContext.Builder().returnFields( new ReturnFieldsImpl( "bar(ABC(*))")).build(), BASEPATH + "/" + methodName + "/" + id + "/foo");

        System.err.println( "result: " + result );

        // verify we are getting back the object which was deleted
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo("foo");
        ResourceState childResourceState = (ResourceState) result.getProperty("bar");
        assertThat(childResourceState.getProperty("ABC")).isNotNull();
        ResourceState grandchildResourceState = (ResourceState) childResourceState.getProperty("ABC");
        assertThat(grandchildResourceState.getProperty("123")).isEqualTo("XYZ");

        // check that it got deleted in the db
        DBObject dbObject = db.getCollection(methodName).findOne(new BasicDBObject("_id", new ObjectId(id)));
        assertThat(dbObject).isNotNull();
        assertThat(dbObject.get("foo")).isNull();
    }

        @Test
    public void testDeleteNonExistantCollection() throws Exception {
        String methodName = "testDeleteNonExistantCollection";
        assertThat(db.getCollectionNames().contains(methodName)).isFalse();

        try {
            ResourceState result = connector.delete(new RequestContext.Builder().build(), BASEPATH + "/" + methodName);
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

        ResourceState result = connector.delete(new RequestContext.Builder().build(), BASEPATH + "/" + methodName);

        System.out.println("RESULT : " + result);

        // check that it was actually deleted
        assertThat(db.getCollectionNames().contains(methodName)).isFalse();
    }

    @Test
    public void testDeleteInvalidId() throws Exception {
        String methodName = "testDeleteInvalidId";
        assertThat(db.getCollectionNames().contains(methodName)).isFalse();

        try {
            ResourceState result = connector.delete(new RequestContext.Builder().build(), BASEPATH + "/foobar123");
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
            ResourceState result = connector.delete(new RequestContext.Builder().build(), BASEPATH + "/" + methodName + "/" + id.toString());
            Fail.fail("shouldn't get here");
        } catch (ResourceNotFoundException e) {
            // expected
        }

    }

}
