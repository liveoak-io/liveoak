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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.mongodb.BasicDBObject;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.bson.types.ObjectId;
import org.junit.Test;

import static org.junit.Assert.*;

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

        assertEquals(id, jsonNode.get("id").asText());
        assertEquals("bar", jsonNode.get("foo").asText());
        assertNotNull(jsonNode.get("self"));
        assertEquals("/storage/testSimpleGet/" + id, jsonNode.get("self").get("href").asText());
    }

    @Test
    public void testComplexGet() throws Exception {
        String methodName = "testComplexGet";
        assertFalse(db.collectionExists(methodName));

        // create the object using the mongo driver directly
        BasicDBObject object = new BasicDBObject();
        object.append("foo", "bar").append("abc", "123").append("obj", new BasicDBObject("foo2", "bar2"));
        object.append("child", new BasicDBObject("grandchild", new BasicDBObject("foo3", "bar3")));
        db.getCollection(methodName).insert(object);
        assertEquals(1, db.getCollection(methodName).getCount());
        String id = object.getObjectId("_id").toString();

        CloseableHttpResponse response = testSimpleGetMethod(baseURL + "/" + methodName + "/" + id + "/child");
        assertEquals(200, response.getStatusLine().getStatusCode());

        // verify response
        ObjectMapper mapper = new ObjectMapper();

        ObjectWriter writer = mapper.writer().withDefaultPrettyPrinter();
        JsonNode jsonNode = mapper.readTree(response.getEntity().getContent());

        System.out.println(writer.writeValueAsString(jsonNode));

        response = testSimpleGetMethod(baseURL + "/" + methodName + "/" + id + "/child/grandchild");
        assertEquals(200, response.getStatusLine().getStatusCode());

        // verify response
         mapper = new ObjectMapper();

        writer = mapper.writer().withDefaultPrettyPrinter();
        jsonNode = mapper.readTree(response.getEntity().getContent());

        //System.out.println(writer.writeValueAsString(jsonNode));


//        assertEquals(3, jsonNode.size()); // id, _self, bar
//        assertEquals(id, jsonNode.get("id").asText());
//        assertEquals("bar", jsonNode.get("foo").asText());
//        assertNotNull(jsonNode.get("_self"));
//        assertEquals("/storage/testSimpleGet/" + id, jsonNode.get("_self").get("href").asText());

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


}
