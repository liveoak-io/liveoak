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
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

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

//        String responseEntity = getEntityAsString(response.getEntity());
//        String id = getId(responseEntity.getBytes());
//        assertEquals("{\"id\":\"" + id + "\",\"foo\":\"bar\"}", responseEntity);

        // verify what is stored in the mongo db
        assertEquals(1, db.getCollection(methodName).getCount());
        DBObject dbObject = db.getCollection(methodName).findOne();
        assertEquals("bar", dbObject.get("foo"));
        //assertEquals(new ObjectId(id), dbObject.get("_id"));
    }


}
