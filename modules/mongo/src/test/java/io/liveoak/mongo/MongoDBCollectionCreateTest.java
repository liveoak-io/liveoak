/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.mongo;

import com.mongodb.BasicDBObject;
import io.liveoak.common.codec.DefaultResourceState;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.exceptions.ResourceAlreadyExistsException;
import io.liveoak.spi.state.ResourceState;
import org.fest.assertions.Fail;
import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 */
public class MongoDBCollectionCreateTest extends BaseMongoDBTest {

    @Test
    public void collectionCreateTests() throws Exception {
        // Test #1 - Create collection
        // check that we can create the resource
        ResourceState state = new DefaultResourceState("movies");
        ResourceState createdResource = client.create(new RequestContext.Builder().build(), "/testApp/storage", state);
        assertThat(createdResource).isNotNull();
        assertThat(createdResource.id()).isEqualTo("movies");

        // test that we get this resource back on a read
        ResourceState movies = client.read(new RequestContext.Builder().build(), "/testApp/storage/movies");
        assertThat(movies).isNotNull();
        assertThat(movies.id()).isEqualTo("movies");


        // Test #2 - Create empty collection
        // DB db = mongoClient.getDB("testGetStorageEmpty");
        db.dropDatabase(); // TODO: create a new DB here instead of dropping the old one ?

        assertThat(db.getCollectionNames()).hasSize(0);

        state = new DefaultResourceState("testCollection");

        ResourceState response = client.create(new RequestContext.Builder().build(), "/testApp/" + BASEPATH, state);

        // verify response
        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo("testCollection");
        assertThat(response.getProperty("type")).isEqualTo("collection");
        assertThat(response.members()).isEmpty();

        // verify whats in mongodb
        assertThat(db.collectionExists("testCollection")).isTrue();
        assertThat(db.getCollection("testCollection").count()).isEqualTo(0);


        // Test #3 - Create collection no id
        // DB db = mongoClient.getDB("testGetStorageEmpty");
        db.dropDatabase(); // TODO: create a new DB here instead of dropping the old one ?
        assertThat(db.getCollectionNames()).hasSize(0);

        response = client.create(new RequestContext.Builder().build(), "/testApp/" + BASEPATH, new DefaultResourceState());

        // verfiy response
        assertThat(response).isNotNull();
        assertThat(response.id()).isNotNull();
        assertThat(response.getProperty("type")).isEqualTo("collection");
        assertThat(response.members()).isEmpty();
        String id = response.id();

        // verify whats in mongodb
        assertThat(db.collectionExists(id)).isTrue();
        assertThat(db.getCollection(id).count()).isEqualTo(0);


        // Test #4 - Create already existing
        db.dropDatabase(); // TODO: create a new DB here instead of dropping the old one ?
        assertThat(db.collectionExists("foobar")).isFalse();
        // create a collection
        db.createCollection("foobar", new BasicDBObject());
        assertThat(db.collectionExists("foobar")).isTrue();

        try {
            client.create(new RequestContext.Builder().build(), "/testApp/" + BASEPATH, new DefaultResourceState("foobar"));
            Fail.fail("shouldn't get here");
        } catch (ResourceAlreadyExistsException e) {
            // expected
        }

    }
}
