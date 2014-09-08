/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.mongo;

import static org.fest.assertions.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

import java.util.LinkedList;
import java.util.List;

import io.liveoak.common.DefaultReturnFields;
import io.liveoak.spi.LiveOak;
import org.fest.assertions.Fail;
import org.junit.Test;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;

import io.liveoak.spi.NotAcceptableException;
import io.liveoak.spi.Pagination;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.ResourceNotFoundException;
import io.liveoak.spi.Sorting;
import io.liveoak.spi.state.ResourceState;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 */
public class MongoDBCollectionReadTest extends BaseMongoDBTest {

    @Test
    public void testRootFound() throws Exception {
        ResourceState result = client.read(new RequestContext.Builder().build(), "/testApp/storage");
        assertThat(result).isNotNull();
    }

    @Test
    public void testUncreatedCollectionNotFound() throws Exception {
        try {
            client.read(new RequestContext.Builder().build(), "/storage/movies");
            fail("shouldn't get here");
        } catch (ResourceNotFoundException e) {
            assertThat(e.path()).isEqualTo("/storage/movies");
        }
    }

    @Test
    public void testGetStorageEmpty() throws Exception {
        db.dropDatabase(); // TODO: create a new DB here instead of dropping the old one
        assertThat(db.getCollectionNames()).hasSize(0);

        ResourceState result = client.read(new RequestContext.Builder().build(), "/testApp/" + BASEPATH);

        // verify response
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(BASEPATH);
        assertThat(result.getPropertyNames().size()).isEqualTo(2);
        assertThat(result.getProperty("type")).isEqualTo("database");
        assertThat(result.getProperty("count")).isEqualTo(0);
        assertThat(result.members()).isEmpty();
    }

    @Test
    public void testGetStorageCollections() throws Exception {
        db.dropDatabase(); // TODO: create a new DB here instead of dropping the old one
        assertThat(db.getCollectionNames()).hasSize(0);

        // create a couple of collections
        db.createCollection("collection1", new BasicDBObject());
        db.createCollection("collection2", new BasicDBObject());
        db.createCollection("collection3", new BasicDBObject());

        // check that the collections are there (Note: there is an internal index collection, so 4 instead of 3)
        assertThat(db.getCollectionNames()).hasSize(4);

        ResourceState result = client.read(new RequestContext.Builder().returnFields(new DefaultReturnFields("*").withExpand(LiveOak.MEMBERS)).build(), "/testApp/" + BASEPATH);

        // verify response
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(BASEPATH);
        assertThat(result.getProperty("type")).isEqualTo("database");
        assertThat(result.members().size()).isEqualTo(3);

        for (int i = 0; i < result.members().size(); i++) {
            ResourceState member = result.members().get(i);
            assertThat(member.id()).isEqualTo("collection" + (i + 1));

            assertThat(member.getProperty("type")).isEqualTo("collection");
            assertThat(member.members()).isEmpty();
        }
    }

    @Test
    public void testGetEmptyCollection() throws Exception {
        String methodName = "testEmptyCollection";
        // check that the collection really is empty
        assertFalse(db.collectionExists(methodName));

        try {
            ResourceState result = client.read(new RequestContext.Builder().build(), "/testApp/" + BASEPATH + "/" + methodName);
            Fail.fail();
        } catch (ResourceNotFoundException e) {
            // expected
        }

        db.createCollection(methodName, new BasicDBObject());

        ResourceState result = client.read(new RequestContext.Builder().build(), "/testApp/" + BASEPATH + "/" + methodName);

        // verify the result
        assertThat(result.id()).isEqualTo(methodName);

        assertThat(result.getProperty("type")).isEqualTo("collection");
        assertThat(result.members()).isEmpty();

        // check that the collection is still empty
        assertEquals(0, db.getCollection(methodName).getCount());
    }

    @Test
    public void testGetStorageCollectionsPagination() throws Exception {
        // DB db = mongoClient.getDB("testGetStorageCollectionsPagination");
        db.dropDatabase();
        assertEquals(0, db.getCollectionNames().size());
        // create a bunch of collections
        for (int i = 0; i < 1013; i++) {
            db.createCollection(String.format("collection%1$04d", i), new BasicDBObject("count", i));
        }
        // check that the collections are there (Note: there is an internal index collection, so 4 instead of 3)
        assertEquals(1014, db.getCollectionNames().size());

        // This should return 23 collections
        RequestContext requestContext = new RequestContext.Builder().returnFields(new DefaultReturnFields("*").withExpand(LiveOak.MEMBERS))
                .pagination(new SimplePagination(11, 23)).build();
        ResourceState result = client.read(requestContext, "/testApp/" + BASEPATH);

        // verify the result
        assertThat(result.id()).isEqualTo(BASEPATH);
        assertThat(result.getPropertyNames().size()).isEqualTo(2);
        assertThat(result.getProperty("type")).isEqualTo("database");
        assertThat(result.members().size()).isEqualTo(23);

        for (int i = 0; i < result.members().size(); i++) {
            ResourceState member = result.members().get(i);
            assertThat(member.id()).isEqualTo(String.format("collection%1$04d", i + 11));

            assertThat(member.getProperty("type")).isEqualTo("collection");
            assertThat(member.members()).isEmpty();
        }

        // This should return 3 collections as a total number of them is 1013
        requestContext = new RequestContext.Builder().returnFields(new DefaultReturnFields("*").withExpand(LiveOak.MEMBERS)).pagination(new SimplePagination(1010, 20)).build();
        result = client.read(requestContext, "/testApp/" + BASEPATH);

        // verify the result
        assertThat(result.id()).isEqualTo(BASEPATH);
        assertThat(result.getPropertyNames().size()).isEqualTo(2);
        assertThat(result.getProperty("type")).isEqualTo("database");
        assertThat(result.members().size()).isEqualTo(3);

        for (int i = 0; i < result.members().size(); i++) {
            ResourceState member = result.members().get(i);
            assertThat(member.id()).isEqualTo(String.format("collection%1$04d", i + 1010));

            assertThat(member.getProperty("type")).isEqualTo("collection");
            assertThat(member.members()).isEmpty();
        }
    }

    @Test
    public void testGetStorageCollectionsQuery() throws Exception {

        DBCollection collection = db.getCollection("testQueryCollection");
        if (collection != null) {
            collection.drop();
        }
        collection = db.createCollection("testQueryCollection", new BasicDBObject("count", 0));

        // insert data records for the test
        setupPeopleData(collection);
        assertThat(collection.count()).isEqualTo(6);

        // This should return 2 items
        SimpleResourceParams resourceParams = new SimpleResourceParams();
        resourceParams.put("q", "{lastName:{$gt:'E', $lt:'R'}}");
        RequestContext requestContext = new RequestContext.Builder().returnFields(new DefaultReturnFields("*").withExpand(LiveOak.MEMBERS)).resourceParams(resourceParams).build();
        ResourceState result = client.read(requestContext, "/testApp/" + BASEPATH + "/testQueryCollection");

        // verify response
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo("testQueryCollection");
        assertThat(result.getProperty("type")).isEqualTo("collection");
        assertThat(result.members().size()).isEqualTo(2);

        assertThat(result.members().get(0).getPropertyNames().size()).isEqualTo(5);
        assertThat(result.members().get(0).getProperty("name")).isEqualTo("Hans");
        assertThat(result.members().get(1).getPropertyNames().size()).isEqualTo(5);
        assertThat(result.members().get(1).getProperty("name")).isEqualTo("Francois");

        // Try another query
        resourceParams = new SimpleResourceParams();
        resourceParams.put("q", "{lastName:'Doe'}");
        requestContext = new RequestContext.Builder().returnFields(new DefaultReturnFields("*").withExpand(LiveOak.MEMBERS)).resourceParams(resourceParams).build();
        result = client.read(requestContext, "/testApp/" + BASEPATH + "/testQueryCollection");

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo("testQueryCollection");
        assertThat(result.getProperty("type")).isEqualTo("collection");
        assertThat(result.members().size()).isEqualTo(2);

        assertThat(result.members().get(0).getPropertyNames().size()).isEqualTo(5);
        assertThat(result.members().get(0).getProperty("name")).isEqualTo("John");
        assertThat(result.members().get(1).getPropertyNames().size()).isEqualTo(5);
        assertThat(result.members().get(1).getProperty("name")).isEqualTo("Jane");
    }

    @Test
    public void testGetStorageCollectionsQueryNoResult() throws Exception {
        DBCollection collection = db.getCollection("testQueryCollectionNoResults");
        if (collection != null) {
            collection.drop();
        }
        collection = db.createCollection("testQueryCollectionNoResults", new BasicDBObject("count", 0));

        // insert data records for the test
        setupPeopleData(collection);
        assertThat(collection.count()).isEqualTo(6);

        // This should return 2 items
        SimpleResourceParams resourceParams = new SimpleResourceParams();
        resourceParams.put("q", "{lastName:\"foo\"}");
        RequestContext requestContext = new RequestContext.Builder().returnFields(new DefaultReturnFields("*").withExpand(LiveOak.MEMBERS)).resourceParams(resourceParams).build();
        ResourceState result = client.read(requestContext, "/testApp/" + BASEPATH + "/testQueryCollectionNoResults");

        // verify response
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo("testQueryCollectionNoResults");

        assertThat(result.getProperty("type")).isEqualTo("collection");
        assertThat(result.members().size()).isEqualTo(0);
    }

    @Test
    public void testGetStorageCollectionsInvalidQueryString() throws Exception {
        DBCollection collection = db.getCollection("testQueryCollectionInvalid");
        if (collection != null) {
            collection.drop();
        }
        collection = db.createCollection("testQueryCollectionInvalid", new BasicDBObject("count", 0));

        // insert data records for the test
        setupPeopleData(collection);
        assertThat(collection.count()).isEqualTo(6);

        // This should return 2 items
        SimpleResourceParams resourceParams = new SimpleResourceParams();
        resourceParams.put("q", "{lastName,\"foo\"}");
        RequestContext requestContext = new RequestContext.Builder().returnFields(new DefaultReturnFields("*").withExpand(LiveOak.MEMBERS)).resourceParams(resourceParams).build();

        try {
            ResourceState result = client.read(requestContext, "/testApp/" + BASEPATH + "/testQueryCollectionInvalid");
            Fail.fail();
        } catch (NotAcceptableException iee) {
            // TODO fix me
            //assertThat(iee.message()).isEqualTo("Invalid JSON format for the 'query' parameter");
            //assertThat(iee.getCause().getClass().getName()).isEqualTo("com.mongodb.util.JSONParseException");
        }
    }

    @Test
    public void testGetStorageCollectionsQueryWithHinting() throws Exception {

        DBCollection collection = db.getCollection("testGetStorageCollectionsQueryWithHinting");
        if (collection != null) {
            collection.drop();
        }
        collection = db.createCollection("testQueryCollection", new BasicDBObject("count", 0));

        // insert data records for the test
        setupPeopleData(collection);
        assertThat(collection.count()).isEqualTo(6);

        // This should return 2 items
        SimpleResourceParams resourceParams = new SimpleResourceParams();
        resourceParams.put("q", "{lastName:{$gt:'E', $lt:'R'}}");
        resourceParams.put("hint", "_id_");

        RequestContext requestContext = new RequestContext.Builder().returnFields(new DefaultReturnFields("*").withExpand(LiveOak.MEMBERS)).resourceParams(resourceParams).build();
        ResourceState result = client.read(requestContext, "/testApp/" + BASEPATH + "/testQueryCollection");

        // verify response
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo("testQueryCollection");

        assertThat(result.getProperty("type")).isEqualTo("collection");
        assertThat(result.members().size()).isEqualTo(2);

        assertThat(result.members().get(0).getPropertyNames().size()).isEqualTo(5);
        assertThat(result.members().get(0).getProperty("name")).isEqualTo("Hans");
        assertThat(result.members().get(1).getPropertyNames().size()).isEqualTo(5);
        assertThat(result.members().get(1).getProperty("name")).isEqualTo("Francois");

        // Try another query
        resourceParams = new SimpleResourceParams();
        resourceParams.put("q", "{lastName:'Doe'}");
        requestContext = new RequestContext.Builder().returnFields(new DefaultReturnFields("*").withExpand(LiveOak.MEMBERS)).resourceParams(resourceParams).build();
        result = client.read(requestContext, "/testApp/" + BASEPATH + "/testQueryCollection");

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo("testQueryCollection");

        assertThat(result.getProperty("type")).isEqualTo("collection");
        assertThat(result.members().size()).isEqualTo(2);

        assertThat(result.members().get(0).getPropertyNames().size()).isEqualTo(5);
        assertThat(result.members().get(0).getProperty("name")).isEqualTo("John");
        assertThat(result.members().get(1).getPropertyNames().size()).isEqualTo(5);
        assertThat(result.members().get(1).getProperty("name")).isEqualTo("Jane");
    }

    @Test
    public void testQueryNonExistantIndex() throws Exception {

        DBCollection collection = db.getCollection("testQueryNonExistantIndex");
        if (collection != null) {
            collection.drop();
        }
        collection = db.createCollection("testQueryCollection", new BasicDBObject("count", 0));

        // insert data records for the test
        setupPeopleData(collection);
        assertThat(collection.count()).isEqualTo(6);

        // This should return 2 items
        SimpleResourceParams resourceParams = new SimpleResourceParams();
        resourceParams.put("q", "{lastName:{$gt:'E', $lt:'R'}}");
        resourceParams.put("hint", "foobar"); // NOTE: foobar does not correspond to an index we can use

        RequestContext requestContext = new RequestContext.Builder().returnFields(new DefaultReturnFields("*").withExpand(LiveOak.MEMBERS)).resourceParams(resourceParams).build();

        try {
            client.read(requestContext, "/testApp/" + BASEPATH + "/testQueryCollection");
            Fail.fail();
        } catch (NotAcceptableException iee) {
            // TODO fix me
            //assertThat(iee.message()).isEqualTo("Exception encountered trying to fetch data from the Mongo Database");

            // Note: tying the test results to the internal mechanisms of Mongo is not a good idea, but
            // its the only way to make sure that the exception is because of the failure we want.
            //assertThat(iee.getCause().getClass().getName()).isEqualTo("com.mongodb.MongoException");
            //assertThat(iee.getCause().getMessage()).isEqualTo("bad hint");
        }
    }

    @Test
    public void testQueryMalformedIndex() throws Exception {

        DBCollection collection = db.getCollection("testQueryMalformedIndex");
        if (collection != null) {
            collection.drop();
        }
        collection = db.createCollection("testQueryCollection", new BasicDBObject("count", 0));

        // insert data records for the test
        setupPeopleData(collection);
        assertThat(collection.count()).isEqualTo(6);

        // This should return 2 items
        SimpleResourceParams resourceParams = new SimpleResourceParams();
        resourceParams.put("q", "{lastName:{$gt:'E', $lt:'R'}}");
        resourceParams.put("hint", "{foobar, 1}"); // NOTE: foobar does not correspond to an index we can use

        RequestContext requestContext = new RequestContext.Builder().returnFields(new DefaultReturnFields("*").withExpand(LiveOak.MEMBERS)).resourceParams(resourceParams).build();

        try {
            client.read(requestContext, "/testApp/" + BASEPATH + "/testQueryCollection");
            Fail.fail();
        } catch (NotAcceptableException iee) {
            // TODO fix this
            //assertThat(iee.message()).isEqualTo("Invalid JSON format for the 'hint' parameter");
            //assertThat(iee.getCause().getClass().getName()).isEqualTo("com.mongodb.util.JSONParseException");
        }
    }

    @Test
    public void testGetStorageCollectionsSort() throws Exception {

        DBCollection collection = db.getCollection("testSortCollection");
        if (collection != null) {
            collection.drop();
        }
        collection = db.createCollection("testSortCollection", new BasicDBObject("count", 0));

        // insert data records for the test
        setupPeopleData(collection);
        assertThat(collection.count()).isEqualTo(6);

        // going through DirectConnector will bypass phase where container sets up Pagination, Sorting, ReturnFields
        // so resourceParams are only relevant for 'q' parameter
        SimpleResourceParams resourceParams = new SimpleResourceParams();

        // This should return 6 items ordered by lastName ascending, and name descending
        RequestContext requestContext = new RequestContext.Builder()
                .returnFields(new DefaultReturnFields("*").withExpand(LiveOak.MEMBERS))
                .sorting(new Sorting("lastName,-name"))
                .resourceParams(resourceParams).build();
        ResourceState result = client.read(requestContext, "/testApp/" + BASEPATH + "/testSortCollection");

        String[] expected = { "Jacqueline", "John", "Jane", "Hans", "Francois", "Helga" };
        assertThat(expected).isEqualTo(getNames(result));
    }

    @Test
    public void testGetStorageCollectionsQueryAndSort() throws Exception {

        DBCollection collection = db.getCollection("testQuerySortCollection");
        if (collection != null) {
            collection.drop();
        }
        collection = db.createCollection("testQuerySortCollection", new BasicDBObject("count", 0));

        // insert data records for the test
        setupPeopleData(collection);
        assertThat(collection.count()).isEqualTo(6);

        // going through DirectConnector will bypass phase where container sets up Pagination, Sorting, ReturnFields
        // so resourceParams are only relevant for 'q' parameter
        SimpleResourceParams resourceParams = new SimpleResourceParams();
        resourceParams.put("q", "{country:{$ne:'FR'}}");

        // This should return 4 items ordered by lastName descending and name ascending
        RequestContext requestContext = new RequestContext.Builder()
                .returnFields(new DefaultReturnFields("*").withExpand(LiveOak.MEMBERS))
                .sorting(new Sorting("-lastName,name"))
                .resourceParams(resourceParams).build();
        ResourceState result = client.read(requestContext, "/testApp/" + BASEPATH + "/testQuerySortCollection");

        String[] expected = { "Helga", "Hans", "Jane", "John" };
        assertThat(expected).isEqualTo(getNames(result));
    }

    @Test
    public void testGetExpandCollectionQuery() throws Exception {

        DBCollection collection = db.getCollection("testExpandQueryCollection");
        if (collection != null) {
            collection.drop();
        }
        collection = db.createCollection("testExpandQueryCollection", new BasicDBObject("count", 0));

        // insert data records for the test
        setupPeopleData(collection);
        assertThat(collection.count()).isEqualTo(6);

        // This should return 3 items
        //
        SimpleResourceParams resourceParams = new SimpleResourceParams();
        RequestContext requestContext = new RequestContext.Builder()
                .returnFields(new DefaultReturnFields("*").withExpand(LiveOak.MEMBERS))
                .resourceParams(resourceParams)
                .build();

        ResourceState result = client.read(requestContext, "/testApp/" + BASEPATH + "/testExpandQueryCollection");

        // verify response
        assertThat(result).isNotNull();

        assertThat(result.id()).isEqualTo("testExpandQueryCollection");
        assertThat(result.members()).isNotNull();

        List results = (List) result.members();
        assertThat(results.size()).isEqualTo(6);

        Object item = results.get(0);
        assertThat(item).isInstanceOf(ResourceState.class);
        ResourceState person = (ResourceState) item;
        assertThat(person.getProperty("identity")).isNotNull();
        assertThat(person.getProperty("identity")).isInstanceOf(ResourceState.class);
        ResourceState identity = (ResourceState) person.getProperty("identity");
        assertThat(identity.id()).isNull();
        assertThat(identity.uri()).isNull();
        assertThat(identity.getProperty("type")).isNotNull();
        assertThat(identity.getProperty("type")).isInstanceOf(String.class);
        assertThat(identity.getProperty(LiveOak.ID)).isNotNull();
        assertThat(identity.getProperty(LiveOak.ID)).isInstanceOf(String.class);
    }

    private String[] getNames(ResourceState result) {
        List<String> ret = new LinkedList<>();
        for (ResourceState item : result.members()) {
            ret.add((String) item.getProperty("name"));
        }
        return ret.toArray(new String[ret.size()]);
    }

    private class SimplePagination implements Pagination {

        int offset;
        int limit;

        public SimplePagination(int offset, int limit) {
            this.offset = offset;
            this.limit = limit;
        }

        @Override
        public int offset() {
            return offset;
        }

        @Override
        public int limit() {
            return limit;
        }
    }
}
