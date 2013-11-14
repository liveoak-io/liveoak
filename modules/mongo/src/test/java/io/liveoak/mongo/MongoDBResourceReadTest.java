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
import com.mongodb.DBObject;
import io.liveoak.container.codec.DefaultResourceState;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.ResourceNotFoundException;
import io.liveoak.spi.state.ResourceState;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.bson.types.ObjectId;
import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;
import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 */
public class MongoDBResourceReadTest extends NewBaseMongoDBTest {

    @Test
    public void testGetSimple() throws Exception {
        String methodName = "testSimpleGet";
        assertFalse(db.collectionExists(methodName));

        // create the object using the mongo driver directly
        BasicDBObject object = new BasicDBObject();
        object.append("foo", "bar");
        db.getCollection(methodName).insert(object);
        assertEquals(1, db.getCollection(methodName).getCount());
        String id = object.getObjectId("_id").toString();

        ResourceState result = connector.read(new RequestContext.Builder().build(), BASEPATH + "/" + methodName + "/" + id);

        //verify response
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(id);
        assertThat(result.getProperty("foo")).isEqualTo("bar");
    }

    @Test
    public void testGetChild() throws Exception {
        String methodName = "testComplexGet";
        assertFalse(db.collectionExists(methodName));

        // create the object using the mongo driver directly
        BasicDBObject object = new BasicDBObject();
        object.append("foo", "bar").append("abc", "123").append("obj", new BasicDBObject("foo2", "bar2"));
        object.append("child", new BasicDBObject("grandchild", new BasicDBObject("foo3", "bar3")));
        db.getCollection(methodName).insert(object);
        assertEquals(1, db.getCollection(methodName).getCount());
        String id = object.getObjectId("_id").toString();

        ResourceState result = connector.read(new RequestContext.Builder().build(), BASEPATH + "/" + methodName + "/" + id + "/child");

        //verify response
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo("child");
        ResourceState grandChild = (ResourceState) result.getProperty("grandchild");
        assertThat(grandChild).isNotNull();
        assertThat(grandChild.id()).isEqualTo("grandchild");
        assertThat(grandChild.getProperty("foo3")).isEqualTo("bar3");

        result = connector.read(new RequestContext.Builder().build(), BASEPATH + "/" + methodName + "/" + id + "/child/grandchild");
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo("grandchild");
        assertThat(result.getProperty("foo3")).isEqualTo("bar3");
    }

    @Test
    public void testGetInvalidId() throws Exception {
        String methodName = "testGetInvalidId";
        assertFalse(db.collectionExists(methodName));

        try {
            ResourceState result = connector.read(new RequestContext.Builder().build(), BASEPATH + "/" + methodName + "/foobar123");
            fail( "shouldn't get here" );
        } catch (ResourceNotFoundException rnfe) {
            // expected
        }
    }

    @Test
    public void testGetNonExistantId() throws Exception {
        String methodName = "testGetNonExistantId";
        assertFalse(db.collectionExists(methodName));

        ObjectId id = new ObjectId();

        try {
            ResourceState result = connector.read(new RequestContext.Builder().build(), BASEPATH + "/" + methodName + "/" + id);
            fail( "shouldn't get here" );
        } catch (ResourceNotFoundException rnfe) {
            // expected
        }
    }


}
