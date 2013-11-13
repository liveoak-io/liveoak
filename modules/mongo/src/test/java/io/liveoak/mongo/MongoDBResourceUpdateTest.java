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
import io.liveoak.container.codec.DefaultResourceState;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.resource.async.Resource;
import io.liveoak.spi.state.ResourceState;
import org.bson.types.ObjectId;
import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 */
public class MongoDBResourceUpdateTest extends NewBaseMongoDBTest {

    @Test
    public void testSimpleUpdate() throws Exception {
        String methodName = "testSimpleDelete";
        assertThat(db.getCollection(methodName).getCount()).isEqualTo(0);

        // create the object using the mongo driver directly
        BasicDBObject object = new BasicDBObject();
        object.append("foo", "bar");
        db.getCollection(methodName).insert(object);
        assertEquals(1, db.getCollection(methodName).getCount());
        String id = object.getObjectId("_id").toString();

        // update the resource using the connector.update method
        ResourceState resourceState = new DefaultResourceState();
        resourceState.putProperty("foo", "baz");

        ResourceState result = connector.update(new RequestContext.Builder().build(), BASEPATH + "/" + methodName + "/" + id, resourceState);

        // verify the result
        // NOTE: if the connector returned a resource state instead of a resource, it would be much easier to test here...
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(id);
        assertThat(result.getProperty("foo")).isEqualTo("baz");

        // verify db content
        assertThat(db.getCollection(methodName).getCount()).isEqualTo(1);
        DBObject dbObject = db.getCollection(methodName).findOne();
        assertEquals("baz", dbObject.get("foo"));
        assertEquals(new ObjectId(id), dbObject.get("_id"));
    }
}
