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

package org.projectodd.restafari.mongo;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.BasicDBObject;

import com.mongodb.DBCollection;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.bson.types.ObjectId;
import org.junit.Test;

import java.net.URLEncoder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 */
public class MongoDBResourceReadTest extends BaseMongoDBTest {

    @Test
    public void testSimpleGet() throws Exception {
        String methodName = "testSimpleGet";
        assertFalse(db.collectionExists(methodName));

        // create the object using the mongo driver directly
        BasicDBObject object = new BasicDBObject();
        object.append("foo", "bar");
        db.getCollection(methodName).insert(object);
        assertEquals(1, db.getCollection(methodName).getCount());
        String id = object.getObjectId("_id").toString();

        CloseableHttpResponse response = testSimpleGetMethod(baseURL + "/" + methodName + "/" + id);
        assertEquals(200, response.getStatusLine().getStatusCode());

        // verify response
        ObjectMapper mapper = new ObjectMapper();

        JsonNode jsonNode = mapper.readTree(response.getEntity().getContent());

        assertEquals(4, jsonNode.size()); // id, _self, bar
        assertEquals(id, jsonNode.get("id").asText());
        assertEquals("bar", jsonNode.get("foo").asText());
        assertNotNull(jsonNode.get("_self"));
        assertEquals("/storage/testSimpleGet/" + id, jsonNode.get("_self").get("href").asText());
    }

    @Test
    public void testGetInvalidId() throws Exception {
        String methodName = "testGetInvalidId";
        assertFalse(db.collectionExists(methodName));

        CloseableHttpResponse response = testSimpleGetMethod(baseURL + "/" + methodName + "/foobar123");
        assertEquals(404, response.getStatusLine().getStatusCode());
    }

    @Test
    public void testGetNonExistantId() throws Exception {
        String methodName = "testGetNonExistantId";
        assertFalse(db.collectionExists(methodName));

        ObjectId id = new ObjectId();

        CloseableHttpResponse response = testSimpleGetMethod(baseURL + "/" + methodName + "/" + id);
        assertEquals(404, response.getStatusLine().getStatusCode());
    }

    @Test
    public void testGetEmptyCollection() throws Exception {
        String methodName = "testEmtpyCollection";
        // check that the collection really is empty
        assertFalse(db.collectionExists(methodName));

        CloseableHttpResponse response = testSimpleGetMethod(baseURL + "/" + methodName);
        // collection does not exist yet, so should return 404
        assertEquals(404, response.getStatusLine().getStatusCode());

        db.createCollection(methodName, new BasicDBObject());
        response = testSimpleGetMethod(baseURL + "/" + methodName);
        assertEquals(200, response.getStatusLine().getStatusCode());

        // check that we get back an empty collection
        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonNode = mapper.readTree(response.getEntity().getContent());

        assertEquals(4, jsonNode.size()); // id, _self, members, _subscriptions
        assertEquals(methodName, jsonNode.get("id").asText());
        assertEquals("{\"href\":\"/storage/testEmtpyCollection\",\"type\":\"collection\"}", jsonNode.get("_self").toString());
        assertEquals("[]", jsonNode.get("content").toString());

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

        // This should return 20 collections
        CloseableHttpResponse response = testSimpleGetMethod(baseURL + "?limit=20");

        assertEquals(200, response.getStatusLine().getStatusCode());

        // verify response
        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonNode = mapper.readTree(response.getEntity().getContent());
        JsonNode content = jsonNode.get("content");

        assertEquals("20 collections limit check", 20, content.size());
        assertEquals("Check first collection is collection0000", "collection0000", content.get(0).get("id").asText());
        assertEquals("Check last collection is collection0019", "collection0019", content.get(19).get("id").asText());
        response.close();

        response = testSimpleGetMethod(baseURL + "?offset=1010&limit=20");
        // This should return 3 collections as a total number of them is 1013
        assertEquals(200, response.getStatusLine().getStatusCode());

        // verify response
        mapper = new ObjectMapper();
        jsonNode = mapper.readTree(response.getEntity().getContent());
        content = jsonNode.get("content");

        assertEquals("Last 3 collections limit check", 3, content.size());
        assertEquals("Check first collection is collection1010", "collection1010", content.get(0).get("id").asText());
        assertEquals("Check last collection is collection1012", "collection1012", content.get(2).get("id").asText());

        // String entity = getEntityAsString(response.getEntity());
        // TODO: verify the entity that gets returned
        // System.out.println("ENTITY : " + entity);
    }


    @Test
    public void testGetStorageCollectionsQuery() throws Exception {

        DBCollection collection = db.getCollection("testQueryCollection");
        if (collection != null) {
            collection.drop();
        }
        collection = db.createCollection("testQueryCollection", new BasicDBObject("count", 0));

        // add a few people
        String [][] data = {
            {"John", "Doe", "US", "San Francisco"},
            {"Jane", "Doe", "US", "New York"},
            {"Hans", "Gruber", "DE", "Berlin"},
            {"Helga", "Schmidt", "DE", "Munich"},
            {"Francois", "Popo", "FR", "Marseille"},
            {"Jacqueline", "Coco", "FR", "Paris"}
        };

        addPeopleItems(collection, data);
        assertEquals("Data set up", 6, collection.count());

        // This should return 2 items
        CloseableHttpResponse response = testSimpleGetMethod(baseURL + "/testQueryCollection?q=" + URLEncoder.encode("{lastName:{$gt:'E', $lt:'R'}}", "utf-8"));

        assertEquals(200, response.getStatusLine().getStatusCode());

        // verify response
        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonNode = mapper.readTree(response.getEntity().getContent());
        JsonNode content = jsonNode.get("content");

        assertEquals("Number of items check", 2, content.size());
        assertEquals("Check first item is Hans", "Hans", content.get(0).get("name").asText());
        assertEquals("Check last item is Francois", "Francois", content.get(1).get("name").asText());
        response.close();


        // This should return 2 items
        response = testSimpleGetMethod(baseURL + "/testQueryCollection?q=" + URLEncoder.encode("{lastName:'Doe'}", "utf-8"));

        assertEquals(200, response.getStatusLine().getStatusCode());

        // verify response
        mapper = new ObjectMapper();
        jsonNode = mapper.readTree(response.getEntity().getContent());
        content = jsonNode.get("content");

        assertEquals("Number of items check", 2, content.size());
        assertEquals("Check first item is John", "John", content.get(0).get("name").asText());
        assertEquals("Check last item is Jane", "Jane", content.get(1).get("name").asText());
        response.close();

    }

    private void addPeopleItems(DBCollection collection, String[][] data) {
        for (String[] rec: data) {
            BasicDBObject obj = new BasicDBObject();
            obj.put("name", rec[0]);
            obj.put("lastName", rec[1]);
            obj.put("country", rec[2]);
            obj.put("city", rec[3]);

            collection.insert(obj);
        }
    }
}
