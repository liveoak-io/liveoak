/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.mongo;

import static org.fest.assertions.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.net.URI;
import java.util.List;

import io.liveoak.common.DefaultReturnFields;
import org.fest.assertions.Fail;
import org.junit.Test;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.DBRef;

import io.liveoak.common.codec.DefaultResourceState;
import io.liveoak.spi.NotAcceptableException;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.ResourceNotFoundException;
import io.liveoak.spi.state.ResourceState;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 */
public class MongoDBRefTest extends BaseMongoDBTest {

    @Test
    public void testReadDBRefSameCollection() throws Exception {
        String methodName = "testReadDBRefSameCollection";
        assertThat(db.collectionExists(methodName)).isFalse();

        DBCollection collection = db.createCollection(methodName, new BasicDBObject());

        // create the object using the mongo driver directly
        BasicDBObject johnObject = new BasicDBObject("_id", "john");
        johnObject.append("name", "John Smith");
        DBRef johnDBRef = new DBRef(db, collection.getName(), johnObject.get("_id"));

        BasicDBObject janeObject = new BasicDBObject("_id", "jane");
        janeObject.append("name", "Jane Smith");
        DBRef janeDBRef = new DBRef(db, collection.getName(), janeObject.get("_id"));

        johnObject.append("spouse", janeDBRef);
        janeObject.append("spouse", johnDBRef);

        collection.insert(johnObject);
        collection.insert(janeObject);
        assertEquals(2, collection.getCount());

        // get the non-expanded, default state
        ResourceState result = client.read(new RequestContext.Builder().build(), "/testApp/" + BASEPATH + "/" + methodName + "/john");
        // verify the non-expanded state
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo("john");
        assertThat(result.uri()).isEqualTo(new URI("/testApp/" + BASEPATH + "/" + methodName + "/john"));
        assertThat(result.getProperty("name")).isEqualTo("John Smith");
        assertThat(result.getProperty("spouse")).isEqualTo(new URI("/testApp/" + BASEPATH + "/" + methodName + "/jane"));

        // get an expanded resource state
        ResourceState expandedResult = client.read(new RequestContext.Builder().returnFields(new DefaultReturnFields("*(*)")).build(), "/testApp/" + BASEPATH + "/" + methodName
                + "/jane");
        // verify this expanded resource state
        assertThat(expandedResult).isNotNull();
        assertThat(expandedResult.id()).isEqualTo("jane");
        assertThat(expandedResult.uri()).isEqualTo(new URI("/testApp/" + BASEPATH + "/" + methodName + "/jane"));
        assertThat(expandedResult.getProperty("name")).isEqualTo("Jane Smith");
        assertThat(expandedResult.getProperty("spouse")).isNotEqualTo(new URI("/testApp/" + BASEPATH + "/" + methodName + "/john"));

        ResourceState expandedSpouse = (ResourceState) expandedResult.getProperty("spouse");
        assertThat(expandedSpouse.id()).isEqualTo("john");
        assertThat(expandedSpouse.uri()).isEqualTo(new URI("/testApp/" + BASEPATH + "/" + methodName + "/john"));
        assertThat(expandedSpouse.getProperty("name")).isEqualTo("John Smith");
        assertThat(expandedSpouse.getProperty("spouse")).isEqualTo(new URI("/testApp/" + BASEPATH + "/" + methodName + "/jane"));

        // get an expanded resource to two levels.
        ResourceState expandedResult2 = client.read(new RequestContext.Builder().returnFields(new DefaultReturnFields("*(*(*))")).build(), "/testApp/" + BASEPATH + "/" + methodName
                + "/jane");
        // verify the results, we should get back the Jane object when accessing the Jane's spouse's spouse
        ResourceState expandedSpouse1 = (ResourceState) expandedResult2.getProperty("spouse");
        ResourceState expandedSpouse2 = (ResourceState) expandedSpouse1.getProperty("spouse");
        assertThat(expandedSpouse2.id()).isEqualTo("jane");
        assertThat(expandedSpouse2.uri()).isEqualTo(new URI("/testApp/" + BASEPATH + "/" + methodName + "/jane"));
        assertThat(expandedSpouse2.getProperty("name")).isEqualTo("Jane Smith");
        assertThat(expandedSpouse2.getProperty("spouse")).isEqualTo(new URI("/testApp/" + BASEPATH + "/" + methodName + "/john"));
    }

    @Test
    public void testReadDBRefInAnotherCollection() throws Exception {

        assertThat(db.collectionExists("employees")).isFalse();
        DBCollection employeesCollection = db.createCollection("employees", new BasicDBObject());

        assertThat(db.collectionExists("departments")).isFalse();
        DBCollection departmentsCollection = db.createCollection("departments", new BasicDBObject());

        // create the object using the mongo driver directly
        BasicDBObject johnObject = new BasicDBObject("_id", "john");
        johnObject.append("firstName", "John");
        johnObject.append("lastName", "Smith");
        DBRef johnDBRef = new DBRef(db, employeesCollection.getName(), johnObject.get("_id"));

        BasicDBObject financingObject = new BasicDBObject("_id", "financing");
        financingObject.append("name", "Financing");
        financingObject.append("head", johnDBRef);
        DBRef departmentDBRef = new DBRef(db, departmentsCollection.getName(), financingObject.get("_id"));

        johnObject.append("department", departmentDBRef);
        financingObject.append("head", johnDBRef);

        employeesCollection.insert(johnObject);
        departmentsCollection.insert(financingObject);
        assertEquals(1, employeesCollection.getCount());
        assertEquals(1, departmentsCollection.getCount());

        ResourceState result = client.read(new RequestContext.Builder().build(), "/testApp/" + BASEPATH + "/employees" + "/john");
        // verify the non-expanded results
        assertThat(result.id()).isEqualTo("john");
        assertThat(result.uri()).isEqualTo(new URI("/testApp/" + BASEPATH + "/employees" + "/john"));
        assertThat(result.getProperty("firstName")).isEqualTo("John");
        assertThat(result.getProperty("lastName")).isEqualTo("Smith");
        assertThat(result.getProperty("department")).isEqualTo(new URI("/testApp/" + BASEPATH + "/departments/financing"));

        ResourceState expandedResult = client.read(new RequestContext.Builder().returnFields(new DefaultReturnFields("*(*)")).build(), "/testApp/" + BASEPATH + "/employees" + "/john");
        // verify the expanded results
        assertThat(expandedResult.id()).isEqualTo("john");
        assertThat(expandedResult.uri()).isEqualTo(new URI("/testApp/" + BASEPATH + "/employees" + "/john"));
        assertThat(expandedResult.getProperty("firstName")).isEqualTo("John");
        assertThat(expandedResult.getProperty("lastName")).isEqualTo("Smith");

        ResourceState departmentState = (ResourceState) expandedResult.getProperty("department");
        assertThat(departmentState.id()).isEqualTo("financing");
        assertThat(departmentState.uri()).isEqualTo(new URI("/testApp/" + BASEPATH + "/departments/financing"));
        assertThat(departmentState.getProperty("head")).isEqualTo(new URI("/testApp/" + BASEPATH + "/employees" + "/john"));
    }

    @Test
    public void testReadNonExistentDBRef() throws Exception {
        String methodName = "testReadNonExistentDBRef";
        assertThat(db.collectionExists(methodName)).isFalse();

        DBCollection collection = db.createCollection(methodName, new BasicDBObject());

        // create the object using the mongo driver directly
        BasicDBObject johnObject = new BasicDBObject("_id", "john");
        johnObject.append("name", "John Smith");

        DBRef janeDBRef = new DBRef(db, collection.getName(), "jane");

        johnObject.append("spouse", janeDBRef);

        collection.insert(johnObject);

        assertEquals(1, collection.getCount());

        // Note: the 'jane' document does _not_ exist in the collection.
        // But we should still be able to retrieve the 'john' document which contains a link which would result in a 404
        // If we try and expand this object, then we should receive an exception

        ResourceState result = client.read(new RequestContext.Builder().build(), "/testApp/" + BASEPATH + "/" + methodName + "/john");

        // verify the result
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo("john");
        assertThat(result.uri()).isEqualTo(new URI("/testApp/" + BASEPATH + "/" + methodName + "/john"));
        assertThat(result.getProperty("name")).isEqualTo("John Smith");
        assertThat(result.getProperty("spouse")).isEqualTo(new URI("/testApp/" + BASEPATH + "/" + methodName + "/jane"));

        // verify that the jane url results in a not found exception
        try {
            client.read(new RequestContext.Builder().build(), "/testApp/" + BASEPATH + "/" + methodName + "/jane");
            Fail.fail();
        } catch (ResourceNotFoundException e) {
            // expected
        }

        // try and expand the result, since the 'spouse' object doesn't exist an exception should be thrown

        try {
            client.read(new RequestContext.Builder().returnFields(new DefaultReturnFields("*(*)")).build(), "/testApp/" + BASEPATH + "/" + methodName + "/john");
            Fail.fail();
        } catch (NotAcceptableException e) {
            // expected
        }
    }

    @Test
    public void testReadDBRefArray() throws Exception {
        String methodName = "testReadDBRefArray";
        assertThat(db.collectionExists(methodName)).isFalse();

        DBCollection collection = db.createCollection(methodName, new BasicDBObject());

        // create the object using the mongo driver directly
        BasicDBObject johnObject = new BasicDBObject("_id", "john");
        johnObject.append("firstName", "John");
        johnObject.append("lastName", "Smith");
        DBRef johnDBRef = new DBRef(db, collection.getName(), johnObject.get("_id"));

        BasicDBObject jackObject = new BasicDBObject("_id", "jack");
        jackObject.append("firstName", "Jack");
        jackObject.append("lastName", "Smith");
        DBRef jackDBRef = new DBRef(db, collection.getName(), jackObject.get("_id"));

        BasicDBObject judyObject = new BasicDBObject("_id", "judy");
        judyObject.append("firstName", "Judy");
        judyObject.append("lastName", "Smith");
        DBRef judyDBRef = new DBRef(db, collection.getName(), judyObject.get("_id"));

        BasicDBList list = new BasicDBList();
        list.add(jackDBRef);
        list.add(judyDBRef);

        BasicDBObject janeObject = new BasicDBObject("_id", "jane");
        janeObject.append("firstName", "Jane");
        janeObject.append("lastName", "Smith");
        DBRef janeDBRef = new DBRef(db, collection.getName(), janeObject.get("_id"));

        johnObject.append("spouse", janeDBRef);
        johnObject.append("children", list);
        janeObject.append("spouse", johnDBRef);

        collection.insert(johnObject);
        collection.insert(janeObject);
        collection.insert(jackObject);
        collection.insert(judyObject);
        assertEquals(4, collection.getCount());

        // get the non-expanded result
        ResourceState result = client.read(new RequestContext.Builder().build(), "/testApp/" + BASEPATH + "/" + methodName + "/john");
        // verify the non-expanded result
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo("john");
        assertThat(result.uri()).isEqualTo(new URI("/testApp/storage/testReadDBRefArray/john"));
        assertThat(result.getProperty("firstName")).isEqualTo("John");
        assertThat(result.getProperty("lastName")).isEqualTo("Smith");
        List childrenResult = (List) result.getProperty("children");
        assertThat(childrenResult.size()).isEqualTo(2);
        assertThat(childrenResult.get(0)).isEqualTo(new URI("/testApp/storage/testReadDBRefArray/jack"));
        assertThat(childrenResult.get(1)).isEqualTo(new URI("/testApp/storage/testReadDBRefArray/judy"));

        // get the expanded result
        ResourceState expandedResult = client.read(new RequestContext.Builder().returnFields(new DefaultReturnFields("*(*)")).build(), "/testApp/" + BASEPATH + "/" + methodName
                + "/john");
        // verify the expanded result
        assertThat(expandedResult).isNotNull();
        assertThat(expandedResult.id()).isEqualTo("john");
        assertThat(expandedResult.uri()).isEqualTo(new URI("/testApp/storage/testReadDBRefArray/john"));
        assertThat(expandedResult.getProperty("firstName")).isEqualTo("John");
        assertThat(expandedResult.getProperty("lastName")).isEqualTo("Smith");
        childrenResult = (List) expandedResult.getProperty("children");
        assertThat(childrenResult.size()).isEqualTo(2);
        assertThat(childrenResult.get(0)).isNotEqualTo(new URI("/testApp/storage/testReadDBRefArray/jack"));
        assertThat(childrenResult.get(1)).isNotEqualTo(new URI("/testApp/storage/testReadDBRefArray/judy"));

        ResourceState judyResourceState = (ResourceState) childrenResult.get(1);
        assertThat(judyResourceState.id()).isEqualTo("judy");
        assertThat(judyResourceState.uri()).isEqualTo(new URI("/testApp/storage/testReadDBRefArray/judy"));
        assertThat(judyResourceState.getProperty("firstName")).isEqualTo("Judy");
        assertThat(judyResourceState.getProperty("lastName")).isEqualTo("Smith");
    }

    @Test
    public void testCreateWithDBRef() throws Exception {
        String methodName = "testCreateWithDBRef";
        assertThat(db.collectionExists(methodName)).isFalse();

        // Create the reference resource in mongo
        DBCollection collection = db.createCollection(methodName, new BasicDBObject());
        BasicDBObject parentObject = new BasicDBObject("_id", "johnSmith");
        parentObject.append("name", "John Smith");
        collection.insert(parentObject);

        ResourceState state = new DefaultResourceState("judySmith");
        state.putProperty("name", "Judy Smith");
        ResourceState father = new DefaultResourceState();
        father.putProperty("$dbref", "/testApp/" + BASEPATH + "/" + methodName + "/johnSmith");
        state.putProperty("father", father);

        // create a resource with a reference
        ResourceState createdResource = client.create(new RequestContext.Builder().build(), "/testApp/" + BASEPATH + "/" + methodName, state);
        // verify the resource
        assertThat(createdResource).isNotNull();
        assertThat(createdResource.id()).isEqualTo("judySmith");
        assertThat(createdResource.uri()).isEqualTo(new URI("/testApp/" + BASEPATH + "/" + methodName + "/judySmith"));
        assertThat(createdResource.getProperty("father")).isEqualTo(new URI("/testApp/" + BASEPATH + "/" + methodName + "/johnSmith"));

        // verify what is in the database
        DBObject judyDBObject = db.getCollection(methodName).findOne(new BasicDBObject("_id", "judySmith"));
        assertThat(judyDBObject.get("name")).isEqualTo("Judy Smith");
        DBRef fatherDBRef = (DBRef) judyDBObject.get("father");
        assertThat(fatherDBRef.getId()).isEqualTo("johnSmith");
        assertThat(fatherDBRef.getRef()).isEqualTo(methodName);

        // verify that we can get back this object
        ResourceState getResource = client.read(new RequestContext.Builder().build(), "/testApp/" + BASEPATH + "/" + methodName + "/judySmith");
        // verify the resource
        assertThat(getResource).isNotNull();
        assertThat(getResource.id()).isEqualTo("judySmith");
        assertThat(getResource.uri()).isEqualTo(new URI("/testApp/" + BASEPATH + "/" + methodName + "/judySmith"));
        assertThat(getResource.getProperty("father")).isEqualTo(new URI("/testApp/" + BASEPATH + "/" + methodName + "/johnSmith"));

        // verify we can get the father using the reference
        getResource = client.read(new RequestContext.Builder().returnFields(new DefaultReturnFields("*(*)")).build(), "/testApp/" + BASEPATH + "/" + methodName + "/judySmith");
        ResourceState fatherResourceState = (ResourceState) getResource.getProperty("father");
        assertThat(fatherResourceState.getProperty("name")).isEqualTo("John Smith");
    }

    @Test
    public void testCreateWithInvalidDBRef() throws Exception {
        String methodName = "testCreateWithInvalidDBRef";
        assertThat(db.collectionExists(methodName)).isFalse();

        // Create the reference resource in mongo
        DBCollection collection = db.createCollection(methodName, new BasicDBObject());
        BasicDBObject johnObject = new BasicDBObject("_id", "johnSmith");
        johnObject.append("name", "John Smith");
        collection.insert(johnObject);

        ResourceState getResource = client.read(new RequestContext.Builder().build(), "/testApp/" + BASEPATH + "/" + methodName + "/johnSmith");

        ResourceState invalidRef = new DefaultResourceState();
        invalidRef.putProperty("$dbref", "not a path to a url.");

        getResource.putProperty("spouse", invalidRef);

        try {
            client.update(new RequestContext.Builder().build(), "/testApp/" + BASEPATH + "/" + methodName + "/johnSmith", getResource);
            fail();
        } catch (NotAcceptableException e) {
            // expected
        }

        // Note: /storage/ would have been fine, but /storage2 isn't because it not within the same context root
        invalidRef.putProperty("$dbref", "/testApp/storage2/testCreateWithInvalidDBRef/foo123");
        getResource.putProperty("spouse", invalidRef);

        try {
            client.update(new RequestContext.Builder().build(), "/testApp/" + BASEPATH + "/" + methodName + "/johnSmith", getResource);
            fail();
        } catch (NotAcceptableException e) {
            // expected
        }

        // missing a resource id
        invalidRef.putProperty("$dbref", "/testApp/storage/testCreateWithInvalidDBRef");
        getResource.putProperty("spouse", invalidRef);

        try {
            client.update(new RequestContext.Builder().build(), "/testApp/" + BASEPATH + "/" + methodName + "/johnSmith", getResource);
            fail();
        } catch (NotAcceptableException e) {
            // expected
        }

        invalidRef.putProperty("$dbref", "");
        getResource.putProperty("spouse", invalidRef);

        try {
            client.update(new RequestContext.Builder().build(), "/testApp/" + BASEPATH + "/" + methodName + "/johnSmith", getResource);
            fail();
        } catch (NotAcceptableException e) {
            // expected
        }

        invalidRef.putProperty("$dbref", null);
        getResource.putProperty("spouse", invalidRef);

        try {
            client.update(new RequestContext.Builder().build(), "/testApp/" + BASEPATH + "/" + methodName + "/johnSmith", getResource);
            fail();
        } catch (NotAcceptableException e) {
            // expected
        }
    }

    @Test
    public void testUpdateWithDBRef() throws Exception {
        String methodName = "testUpdateWithDBRef";
        assertThat(db.collectionExists(methodName)).isFalse();

        DBCollection people = db.createCollection(methodName, new BasicDBObject());

        // create some objects using the mongo driver directly
        BasicDBObject sallyObject = new BasicDBObject("_id", "Sally");
        sallyObject.append("name", "Sally");
        DBRef sallyDBRef = new DBRef(db, people.getName(), sallyObject.get("_id"));

        BasicDBObject sueObject = new BasicDBObject("_id", "Sue");
        sueObject.append("name", "Sue");
        sueObject.append("bestFriend", sallyDBRef);

        BasicDBObject steveObject = new BasicDBObject("_id", "Steve");
        steveObject.append("name", "Steve");

        people.insert(sallyObject);
        people.insert(sueObject);
        people.insert(steveObject);

        ResourceState sueResource = client.read(new RequestContext.Builder().returnFields(new DefaultReturnFields("*(*)")).build(), "/testApp/" + BASEPATH + "/" + methodName
                + "/Sue");
        // verify the result, with Sally being Sue's best friend
        ResourceState bestFriend = (ResourceState) sueResource.getProperty("bestFriend");
        assertThat(bestFriend.getProperty("name")).isEqualTo("Sally");

        // update the Sue object so that Steve is now her best friend
        DefaultResourceState newBestFriend = new DefaultResourceState();
        newBestFriend.putProperty("$dbref", "/testApp/" + BASEPATH + "/" + methodName + "/Steve");
        sueResource.putProperty("bestFriend", newBestFriend);

        ResourceState updatedState = client.update(new RequestContext.Builder().build(), "/testApp/" + BASEPATH + "/" + methodName + "/Sue", sueResource);
        // verify the result
        assertThat(updatedState.getProperty("bestFriend")).isEqualTo(new URI("/testApp/" + BASEPATH + "/" + methodName + "/Steve"));

        sueResource = client.read(new RequestContext.Builder().build(), "/testApp/" + BASEPATH + "/" + methodName + "/Sue");
        // verify the result, with Steve now being Sue's best frien
        assertThat(sueResource.getProperty("bestFriend")).isEqualTo(new URI("/testApp/" + BASEPATH + "/" + methodName + "/Steve"));

        sueResource = client.read(new RequestContext.Builder().returnFields(new DefaultReturnFields("*(*)")).build(), "/testApp/" + BASEPATH + "/" + methodName + "/Sue");
        // verify the result, with Steve now being Sue's best friend
        bestFriend = (ResourceState) sueResource.getProperty("bestFriend");
        assertThat(bestFriend.getProperty("name")).isEqualTo("Steve");
    }

    @Test
    public void testRemovingDBRef() throws Exception {
        String methodName = "testRemovingDBRef";
        assertThat(db.collectionExists(methodName)).isFalse();

        DBCollection people = db.createCollection(methodName, new BasicDBObject());

        // create some objects using the mongo driver directly
        BasicDBObject sallyObject = new BasicDBObject("_id", "Sally");
        sallyObject.append("name", "Sally");
        DBRef sallyDBRef = new DBRef(db, people.getName(), sallyObject.get("_id"));

        BasicDBObject sueObject = new BasicDBObject("_id", "Sue");
        sueObject.append("name", "Sue");
        sueObject.append("bestFriend", sallyDBRef);

        people.insert(sallyObject);
        people.insert(sueObject);

        ResourceState sueResource = client.read(new RequestContext.Builder().returnFields(new DefaultReturnFields("*(*)")).build(), "/testApp/" + BASEPATH + "/" + methodName
                + "/Sue");
        // verify the result, with Sally being Sue's best friend
        ResourceState bestFriend = (ResourceState) sueResource.getProperty("bestFriend");
        assertThat(bestFriend.getProperty("name")).isEqualTo("Sally");

        // now try and remove the bestFriend value
        sueResource.putProperty("bestFriend", null);

        ResourceState updatedState = client.update(new RequestContext.Builder().build(), "/testApp/" + BASEPATH + "/" + methodName + "/Sue", sueResource);
        assertThat(updatedState.getProperty("bestFriend")).isNull();
    }

    @Test
    public void testOverwriteWithDBRef() throws Exception {
        String methodName = "testUpdateWithDBRef";
        assertThat(db.collectionExists(methodName)).isFalse();

        DBCollection people = db.createCollection(methodName, new BasicDBObject());

        // create some objects using the mongo driver directly
        BasicDBObject sallyObject = new BasicDBObject("_id", "sally");
        sallyObject.append("name", "Sally");
        DBRef sallyDBRef = new DBRef(db, people.getName(), sallyObject.get("_id"));

        BasicDBObject sueObject = new BasicDBObject("_id", "sue");
        sueObject.append("name", "Sue");
        sueObject.append("bestFriend", sallyDBRef);

        people.insert(sallyObject);
        people.insert(sueObject);

        ResourceState sueResource = client.read(new RequestContext.Builder().returnFields(new DefaultReturnFields("*(*)")).build(), "/testApp/" + BASEPATH + "/" + methodName
                + "/sue");
        // verify the result, with Sally being Sue's best friend
        ResourceState bestFriend = (ResourceState) sueResource.getProperty("bestFriend");
        assertThat(bestFriend.uri()).isEqualTo(new URI("/testApp/" + BASEPATH + "/" + methodName + "/sally"));
        assertThat(bestFriend.id()).isEqualTo("sally");
        assertThat(bestFriend.getProperty("name")).isEqualTo("Sally");

        // now try and change the bestFriend value directly
        // Note: the resourceState here is the expanded resource state, if we try and modify the referenced object directly
        // we should get an error.
        bestFriend.putProperty("name", "Steve");

        try {
            client.update(new RequestContext.Builder().build(), "/testApp/" + BASEPATH + "/" + methodName + "/sue", sueResource);
            fail();
        } catch (Exception e) {
            // expected
        }

        // remove the ID from the bestFriend object, this should now make it an embedded object, which is legal for an update
        bestFriend.id(null);
        ResourceState updatedState = client.update(new RequestContext.Builder().build(), "/testApp/" + BASEPATH + "/" + methodName + "/sue", sueResource);

        assertThat(updatedState).isNotNull();
        bestFriend = (ResourceState) updatedState.getProperty("bestFriend");
        assertThat(bestFriend.id()).isNull();
        assertThat(bestFriend.uri()).isNull();
        assertThat(bestFriend.getProperty("name")).isEqualTo("Steve");

        // Verify what is in Mongo
        sallyObject = (BasicDBObject) people.findOne(new BasicDBObject("_id", "sue"));
        assertThat(sallyObject.get("bestFriend")).isInstanceOf(BasicDBObject.class);
        BasicDBObject friendObject = (BasicDBObject) sallyObject.get("bestFriend");
        assertThat(friendObject.get("name")).isEqualTo("Steve");
        assertThat(friendObject.size()).isEqualTo(1);
    }

}
