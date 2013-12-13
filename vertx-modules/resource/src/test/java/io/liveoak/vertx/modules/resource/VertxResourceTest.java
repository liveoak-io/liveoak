/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.vertx.modules.resource;

import io.liveoak.container.LiveOakFactory;
import io.liveoak.container.LiveOakSystem;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.ResourceException;
import io.liveoak.spi.client.Client;
import io.liveoak.spi.resource.async.Resource;
import io.liveoak.spi.resource.async.ResourceSink;
import io.liveoak.spi.state.ResourceState;
import io.liveoak.vertx.modules.server.ResourceDeployer;
import org.junit.Before;
import org.junit.Test;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static org.junit.Assert.fail;
import static org.fest.assertions.Assertions.assertThat;


/**
 * @author Bob McWhirter
 * @author Lance Ball
 */
public class VertxResourceTest {

    private LiveOakSystem system;
    private Client client;
    private ResourceDeployer deployer;

    private Map<String, JsonObject> objects = new HashMap<>();
    private CollectionResourceAdapter adapter;


    @Before
    public void setUpSystem() throws Exception {
        this.system = LiveOakFactory.create();
        this.client = this.system.client();
        this.deployer = new ResourceDeployer(this.system, "test.register");
    }

    @Before
    public void setUpResource() throws Exception {
        this.adapter = new CollectionResourceAdapter(this.system.vertx(), "people", "test.register");

        this.objects.put("bob", new JsonObject().putString("id", "bob").putString("name", "Bob McWhirter"));
        this.objects.put("ben", new JsonObject().putString("id", "ben").putString("name", "Ben Browning"));
        this.adapter.readMemberHandler((id, responder) -> {

            System.err.println("asked for: " + id);
            JsonObject object = objects.get(id);

            if (object != null) {
                responder.resourceRead(object);
            } else {
                responder.noSuchResource(id);
            }
        });

        this.adapter.readMembersHandler((responder) -> {
            JsonArray resources = new JsonArray(objects.values().toArray());
            responder.resourcesRead(resources);
        });

        this.adapter.start();
        Thread.sleep(500);
    }

    @Test
    public void testReadMember() throws Exception {
        ResourceState result = client.read(new RequestContext.Builder().build(), "/people/bob");
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo("bob");
    }


    /*
    @Test
    public void testReadMemberProperty() throws Exception {
        Resource result = client.read("/people/bob/name");
        assertThat(result).isNotNull();
        assertThat(result).isInstanceOf(PropertyResource.class);
        assertThat(((PropertyResource) result).get(null)).isEqualTo("Bob McWhirter");
    }
    */

    @Test
    public void testReadNonExistentResource() {
        try {
            client.read(new RequestContext.Builder().build(), "/people/lance");
        } catch (ResourceException e) {
            assertThat(e.path()).isEqualTo("/people/lance");
        } catch (Exception e) {
            fail("Unexpected exception: " + e);
        }
    }

    @Test
    public void testReadMembers() throws Exception {

        List<Resource> resources = new ArrayList<>();
        CompletableFuture<List<Resource>> future = new CompletableFuture<>();
        ResourceSink sink = new ResourceSink() {
            @Override
            public void close() {
                future.complete(resources);
            }

            @Override
            public void accept(Resource resource) {
                resources.add(resource);
            }
        };

        ResourceState resource = client.read(new RequestContext.Builder().build(), "/people");
        assertThat(resource).isNotNull();
        assertThat(resource.members()).hasSize(2);

    }

}
