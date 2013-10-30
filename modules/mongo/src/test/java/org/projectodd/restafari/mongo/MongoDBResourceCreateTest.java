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
import com.mongodb.DBObject;
import org.apache.commons.io.IOUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.bson.types.ObjectId;
import org.junit.Ignore;
import org.junit.Test;
import sun.nio.ch.IOUtil;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 */
public class MongoDBResourceCreateTest extends BaseMongoDBTest{

    @Test
    public void testSimpleCreate() throws Exception {
        String methodName = "testSimpleCreate";
        assertFalse(db.collectionExists(methodName));
        db.createCollection(methodName, new BasicDBObject());

        CloseableHttpResponse response = testSimplePostMethod(baseURL + "/" + methodName, "{\"foo\":\"bar\"}");

        assertEquals(201, response.getStatusLine().getStatusCode());

        // verify response
        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonNode = mapper.readTree(response.getEntity().getContent());
        assertEquals(3, jsonNode.size()); // id, _self, bar
        assertNotNull(jsonNode.get("id").asText());
        assertEquals("bar", jsonNode.get("foo").asText());
        assertNotNull(jsonNode.get("_self"));

        // verify what is stored in the mongo db
        assertTrue(db.collectionExists(methodName));
        assertEquals(1, db.getCollection(methodName).getCount());
        DBObject dbObject = db.getCollection(methodName).findOne();
        assertEquals("bar", dbObject.get("foo"));
    }

    @Test
    public void testSimpleCreateWithId() throws Exception {
        String methodName = "testSimpleCreateWithID";
        assertFalse(db.collectionExists(methodName));
        db.createCollection(methodName, new BasicDBObject());

        CloseableHttpResponse response = testSimplePostMethod(baseURL + "/" + methodName, "{\"id\":\"helloworld\", \"foo\":\"bar\"}");
        assertEquals(201, response.getStatusLine().getStatusCode());

        // verify response
        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonNode = mapper.readTree(response.getEntity().getContent());
        assertEquals(3, jsonNode.size()); // id, _self, bar
        assertEquals("helloworld", jsonNode.get("id").asText());
        assertEquals("bar", jsonNode.get("foo").asText());
        assertNotNull(jsonNode.get("_self"));

        // verify what is stored in the mongo db
        assertTrue(db.collectionExists(methodName));
        assertEquals(1, db.getCollection(methodName).getCount());
        DBObject dbObject = db.getCollection(methodName).findOne(new BasicDBObject("_id", "helloworld"));
        assertEquals("bar", dbObject.get("foo"));
    }


    @Test
    public void testComplexCreate() throws Exception {
        String methodName = "testComplexCreate";
        assertFalse(db.collectionExists(methodName));
        db.createCollection(methodName, new BasicDBObject());

        CloseableHttpResponse response = testSimplePostMethod(baseURL + "/" + methodName,
                "{ \"id\" : \"helloworld\",\n" +
                        "  \"foo\" : \"bar\",\n" +
                        "  \"test\" : \"123\",\n" +
                        "  \"arr\" : [1, 1, 2, 3, 5, 8, 13, 21],\n" +
                        "  \"obj\" : { \"foo2\" : \"bar2\", \"test2\": \"123\"}\n" +
                        "}\n");
        assertEquals(201, response.getStatusLine().getStatusCode());

        // verify response
        ObjectMapper mapper = new ObjectMapper();
        IOUtils.copy(response.getEntity().getContent(), System.out);

        //System.out.println("MONGO: " + db.getCollection(methodName).findOne());

        CloseableHttpResponse readResponse = testSimpleGetMethod(baseURL + "/" + methodName + "/helloworld/obj");
        //System.out.println("READ: " + readResponse.getStatusLine().toString());
        //IOUtils.copy(readResponse.getEntity().getContent(), System.out);

//        JsonNode jsonNode = mapper.readTree(response.getEntity().getContent());
//        assertEquals(5, jsonNode.size()); // id, _self, bar
//        assertEquals("helloworld", jsonNode.get("id").asText());
//        assertEquals("bar", jsonNode.get("foo").asText());
//        assertEquals("test", jsonNode.get("123").asText());
//        assertEquals("[0,1,2,3,5,8,13]", jsonNode.get("arr").asText());
//        assertNotNull(jsonNode.get("_self"));
//
//        // verify what is stored in the mongo db
//        assertTrue(db.collectionExists(methodName));
//        assertEquals(1, db.getCollection(methodName).getCount());
//        DBObject dbObject = db.getCollection(methodName).findOne();
//        assertEquals("bar", dbObject.get("foo"));
    }


}
