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

import com.mongodb.BasicDBObject;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.bson.types.ObjectId;
import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 */
public class MongoDBResourceDeleteTest extends BaseMongoDBTest{

    @Test
    public void testSimpleDelete() throws Exception {
        String methodName = "testSimpleDelete";
        assertEquals(0, db.getCollection(methodName).getCount());

        // create the object using the mongo driver directly
        BasicDBObject object = new BasicDBObject();
        object.append("foo", "bar");
        db.getCollection(methodName).insert(object);
        assertEquals(1, db.getCollection(methodName).getCount());
        String id = object.getObjectId("_id").toString();

        // now delete the object
        CloseableHttpResponse response = testSimpleDeleteMethod(baseURL + "/" + methodName + "/" + id);
        assertEquals(200, response.getStatusLine().getStatusCode());
        assertEquals(0, db.getCollection(methodName).getCount());
    }

    @Test
    public void testDeleteNonExistantCollection() throws Exception {
        String methodName = "testDeleteNonExistantCollection";
        assertEquals(0, db.getCollection(methodName).getCount());

        CloseableHttpResponse response = testSimpleDeleteMethod(baseURL + "/" + methodName);
        assertEquals(404, response.getStatusLine().getStatusCode());
    }

    @Test
    public void testDeleteCollection() throws Exception {
        String methodName = "testDeleteCollection";
        assertFalse(db.getCollectionNames().contains(methodName));

        // create the collection
        db.createCollection(methodName, new BasicDBObject());

        assertTrue(db.getCollectionNames().contains(methodName));

        CloseableHttpResponse response = testSimpleDeleteMethod(baseURL + "/" + methodName);
        assertEquals(200, response.getStatusLine().getStatusCode());

        // check that it was actually deleted
        assertFalse(db.getCollectionNames().contains(methodName));
    }

    @Test
    public void testDeleteInvalidId() throws Exception {
        String methodName = "testDeleteInvalidId";
        assertEquals(0, db.getCollection(methodName).getCount());

        CloseableHttpResponse response = testSimpleDeleteMethod(baseURL + "/" + methodName + "/foobar123");
        assertEquals(404, response.getStatusLine().getStatusCode());
    }

    @Test
    public void testDeleteNonExistantId() throws Exception {
        String methodName = "testDeleteNonExistantId";
        assertEquals(0, db.getCollection(methodName).getCount());

        ObjectId id = new ObjectId();

        CloseableHttpResponse response = testSimpleDeleteMethod(baseURL + "/" + methodName + "/" + id.toString());
        assertEquals(404, response.getStatusLine().getStatusCode());
    }

}
