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
import org.apache.http.client.methods.CloseableHttpResponse;
import org.bson.types.ObjectId;
import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 */
public class MongoDBResourceUpdateTest extends BaseMongoDBTest {

    @Test
    public void testSimpleUpdate() throws Exception {
        String methodName = "testSimpleDelete";
        assertEquals(0, db.getCollection(methodName).getCount());

        // create the object using the mongo driver directly
        BasicDBObject object = new BasicDBObject();
        object.append("foo", "bar");
        db.getCollection(methodName).insert(object);
        assertEquals(1, db.getCollection(methodName).getCount());
        String id = object.getObjectId("_id").toString();

        // now update the object
        CloseableHttpResponse response = testSimplePutMethod(baseURL + "/" + methodName + "/" + id, "{\"foo\":\"baz\"}");
        assertEquals(200, response.getStatusLine().getStatusCode());

        // verify response
        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonNode = mapper.readTree(response.getEntity().getContent());

        assertEquals(4, jsonNode.size()); // id, _self, bar, _subscriptions
        assertEquals(id, jsonNode.get("id").asText());
        assertEquals("baz", jsonNode.get("foo").asText());
        assertNotNull(jsonNode.get("_self"));
        assertEquals("/storage/testSimpleDelete/" + id, jsonNode.get("_self").get("href").asText());

        // verify db content
        assertEquals(1, db.getCollection(methodName).getCount());
        DBObject dbObject = db.getCollection(methodName).findOne();
        assertEquals("baz", dbObject.get("foo"));
        assertEquals(new ObjectId(id), dbObject.get("_id"));
    }
}
