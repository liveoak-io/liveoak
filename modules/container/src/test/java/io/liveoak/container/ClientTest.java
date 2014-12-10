/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.container;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import io.liveoak.common.DefaultReturnFields;
import io.liveoak.common.codec.DefaultResourceState;
import io.liveoak.container.tenancy.InternalApplication;
import io.liveoak.container.extension.application.InternalApplicationExtension;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.ReturnFields;
import io.liveoak.spi.client.Client;
import io.liveoak.spi.client.ClientResourceResponse;
import io.liveoak.spi.state.ResourceState;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;
import static org.fest.assertions.Fail.fail;

/**
 * @author Bob McWhirter
 */
public class ClientTest extends AbstractContainerTest {

    private static Client client;
    private static InternalApplication application;

    @BeforeClass
    public static void setUp() throws Exception {
        system = LiveOakFactory.create();
        client = system.client();

        awaitStability();

        application = system.applicationRegistry().createApplication("testApp", "Test Application");
        system.extensionInstaller().load("db", new InMemoryDBExtension());
        application.extend("db");

        InMemoryDBResource db = (InMemoryDBResource) system.service(InMemoryDBExtension.resource("testApp", "db"));
        db.addMember(new InMemoryCollectionResource(db, "people"));
        db.addMember(new InMemoryCollectionResource(db, "dogs"));
    }

    @AfterClass
    public static void shutdown() throws Exception {
        system.stop();
    }

    @Test
    public void all() throws Throwable {
        read();
        create();
        update();
        readFromResource();
        readFromBlockingResource();
        simpleAsync();
        nestedAsync();
        nestedAsyncWithFinalSync();
    }

    private void read() throws Throwable {
        ReturnFields fields = new DefaultReturnFields("*,members(*,members(*))");

        RequestContext requestContext = new RequestContext.Builder().returnFields(fields).build();
        ResourceState result = client.read(requestContext, "/testApp");
        assertThat(result).isNotNull();

        List<ResourceState> members = result.members();
        assertThat(members).isNotEmpty();

        ResourceState db = members.stream().filter((e) -> e.id().equals("db")).findFirst().get();
        assertThat(db).isNotNull();

        ResourceState people = db.members().stream().filter((e) -> e.id().equals("people")).findFirst().get();
        assertThat(people).isNotNull();

        ResourceState dogs = db.members().stream().filter((e) -> e.id().equals("dogs")).findFirst().get();
        assertThat(dogs).isNotNull();
    }

    private void create() throws Throwable {
        ReturnFields fields = new DefaultReturnFields("*");
        RequestContext requestContext = new RequestContext.Builder().returnFields(fields).build();
        ResourceState bob = new DefaultResourceState("bob");
        bob.putProperty("name", "Bob McWhirter");

        ResourceState result = client.create(requestContext, "/testApp/db/people", bob);
        assertThat(result).isNotNull();
        assertThat(result.getProperty("name")).isEqualTo("Bob McWhirter");

        ResourceState people = client.read(requestContext, "/testApp/db/people");
        assertThat(people).isNotNull();

        ResourceState foundBob = people.members().stream().filter((e) -> e.id().equals("bob")).findFirst().get();
        assertThat(foundBob).isNotNull();
        assertThat(foundBob.id()).isEqualTo("bob");
        assertThat(foundBob.getPropertyNames()).hasSize(0);

        foundBob = client.read(requestContext, "/testApp/db/people/bob");
        assertThat(foundBob).isNotNull();
        assertThat(foundBob.getProperty("name")).isEqualTo("Bob McWhirter");
    }

    private void update() throws Throwable {
        ReturnFields fields = new DefaultReturnFields("*");
        RequestContext requestContext = new RequestContext.Builder().returnFields(fields).build();

        ResourceState foundBob = client.read(requestContext, "/testApp/db/people/bob");
        assertThat(foundBob).isNotNull();
        assertThat(foundBob.getProperty("name")).isEqualTo("Bob McWhirter");

        ResourceState bob = new DefaultResourceState("bob");
        bob.putProperty("name", "Robert McWhirter");

        ResourceState result = client.update(requestContext, "/testApp/db/people/bob", bob);
        assertThat(result).isNotNull();
        assertThat(result.getProperty("name")).isEqualTo("Robert McWhirter");

        foundBob = client.read(requestContext, "/testApp/db/people/bob");
        assertThat(foundBob).isNotNull();
        assertThat(foundBob.getProperty("name")).isEqualTo("Robert McWhirter");
    }

    private void readFromResource() throws Throwable {
        RequestContext requestContext = new RequestContext.Builder().build();
        ResourceState bob = new DefaultResourceState("proxybob");
        bob.putProperty("name", "Bob McWhirter");
        ResourceState createdBob = client.create(requestContext, "/testApp/db/people", bob);
        assertThat(createdBob).isNotNull();

        system.extensionInstaller().load("proxy", new ProxyExtension());
        InternalApplicationExtension ext = application.extend("proxy", JsonNodeFactory.instance.objectNode().put("blocking", false));
        awaitStability();

        ResourceState result = client.read(requestContext, "/testApp/proxy");
        assertThat(result.getPropertyNames()).contains("name");
        assertThat(result.getProperty("name")).isEqualTo("Bob McWhirter");

        ext.remove();
        awaitStability();
    }

    private void readFromBlockingResource() throws Throwable {
        RequestContext requestContext = new RequestContext.Builder().build();
        ResourceState bob = new DefaultResourceState("blockingproxybob");
        bob.putProperty("name", "Bob McWhirter");
        ResourceState createdBob = client.create(requestContext, "/testApp/db/people", bob);
        assertThat(createdBob).isNotNull();
        InternalApplicationExtension ext = application.extend("proxy", JsonNodeFactory.instance.objectNode().put("blocking", true));
        awaitStability();

        ResourceState result = client.read(requestContext, "/testApp/proxy");
        assertThat(result.getPropertyNames()).contains("name");
        assertThat(result.getProperty("name")).isEqualTo("Bob McWhirter");

        ext.remove();
        awaitStability();
    }

    private void simpleAsync() throws Throwable {
        RequestContext requestContext = new RequestContext.Builder().build();
        CompletableFuture<ClientResourceResponse> future = new CompletableFuture<>();

        client.read(requestContext, "/testApp/db/dogs", future::complete);

        ClientResourceResponse response = future.get();
        assertThat(response).isNotNull();
        assertThat(response.state().id()).isEqualTo("dogs");
    }

    private void nestedAsync() throws Throwable {
        RequestContext reqCtxt = new RequestContext.Builder().build();
        CompletableFuture<ClientResourceResponse> nestedFuture = new CompletableFuture<>();

        client.read(reqCtxt, "/testApp/db/dogs", clientResourceResponse1 -> {
            assertThat(clientResourceResponse1.state().id()).isEqualTo("dogs");

            client.read(reqCtxt, "/testApp/db/people", clientResourceResponse2 -> {
                assertThat(clientResourceResponse2.state().id()).isEqualTo("people");

                client.read(reqCtxt, "/testApp/db/dogs", clientResourceResponse3 -> {
                    assertThat(clientResourceResponse3.state().id()).isEqualTo("dogs");

                    client.read(reqCtxt, "/testApp/db/people", nestedFuture::complete);
                });
            });


        });

        ClientResourceResponse response = nestedFuture.get();
        assertThat(response).isNotNull();
        assertThat(response.state().id()).isEqualTo("people");
    }

    private void nestedAsyncWithFinalSync() throws Throwable {
        RequestContext nestedCtx = new RequestContext.Builder().build();
        CompletableFuture<ResourceState> nestedFuture = new CompletableFuture<>();

        client.read(nestedCtx, "/testApp/db/dogs", clientResourceResponse1 -> {
            assertThat(clientResourceResponse1.state().id()).isEqualTo("dogs");

            client.read(nestedCtx, "/testApp/db/people", clientResourceResponse2 -> {
                assertThat(clientResourceResponse2.state().id()).isEqualTo("people");

                client.read(nestedCtx, "/testApp/db/dogs", clientResourceResponse3 -> {
                    assertThat(clientResourceResponse3.state().id()).isEqualTo("dogs");

                    try {
                        nestedFuture.complete(client.read(nestedCtx, "/testApp/db/people"));
                    } catch (Exception e) {
                        e.printStackTrace();
                        fail();
                    }
                });
            });
        });

        ResourceState state = nestedFuture.get();

        assertThat(state).isNotNull();
        assertThat(state.id()).isEqualTo("people");
    }

}
