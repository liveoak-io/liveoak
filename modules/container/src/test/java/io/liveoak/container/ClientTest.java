/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.container;

import java.util.List;

import io.liveoak.common.codec.DefaultResourceState;
import io.liveoak.spi.Container;
import io.liveoak.spi.InitializationException;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.ResourceContext;
import io.liveoak.spi.ReturnFields;
import io.liveoak.spi.client.Client;
import io.liveoak.spi.resource.BlockingResource;
import io.liveoak.spi.resource.RootResource;
import io.liveoak.spi.resource.async.PropertySink;
import io.liveoak.spi.state.ResourceState;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;

/**
 * @author Bob McWhirter
 */
public class ClientTest {

    private LiveOakSystem system;
    private Container container;
    private Client client;


    @Before
    public void setUp() throws Exception {
        this.system = LiveOakFactory.create();
        this.container = this.system.container();
        this.client = this.system.client();

        InMemoryDBResource db = new InMemoryDBResource("db");
        db.addMember(new InMemoryCollectionResource(db, "people"));
        db.addMember(new InMemoryCollectionResource(db, "dogs"));

        this.container.registerResource(db);
    }

    @After
    public void shutdown() throws Exception {
        this.system.stop();
    }

    @Test
    public void testRead() throws Throwable {

        ReturnFields fields = new ReturnFieldsImpl("*,members(*,members(*))");

        RequestContext requestContext = new RequestContext.Builder().returnFields(fields).build();
        ResourceState result = this.client.read(requestContext, "/");
        assertThat(result).isNotNull();

        System.err.println("result: " + result);

        List<ResourceState> members = result.members();

        assertThat(members).isNotEmpty();

        ResourceState db = members.stream().filter((e) -> e.id().equals("db")).findFirst().get();
        assertThat(db).isNotNull();

        ResourceState people = db.members().stream().filter((e) -> e.id().equals("people")).findFirst().get();
        assertThat(people).isNotNull();

        ResourceState dogs = db.members().stream().filter((e) -> e.id().equals("dogs")).findFirst().get();
        assertThat(dogs).isNotNull();
    }

    @Test
    public void testCreate() throws Throwable {

        ReturnFields fields = new ReturnFieldsImpl("*");

        RequestContext requestContext = new RequestContext.Builder().returnFields(fields).build();
        ResourceState bob = new DefaultResourceState("bob");
        bob.putProperty("name", "Bob McWhirter");

        ResourceState result = this.client.create(requestContext, "/db/people", bob);

        assertThat(result).isNotNull();
        assertThat(result.getProperty("name")).isEqualTo("Bob McWhirter");

        ResourceState people = this.client.read(requestContext, "/db/people");

        assertThat(people).isNotNull();

        System.err.println(people);

        ResourceState foundBob = people.members().stream().filter((e) -> e.id().equals("bob")).findFirst().get();
        assertThat(foundBob).isNotNull();
        assertThat(foundBob.id()).isEqualTo("bob");
        assertThat(foundBob.getPropertyNames()).hasSize(0);

        foundBob = this.client.read(requestContext, "/db/people/bob");

        assertThat(foundBob).isNotNull();
        assertThat(foundBob.getProperty("name")).isEqualTo("Bob McWhirter");
    }

    @Test
    public void testUpdate() throws Throwable {
        RequestContext requestContext = new RequestContext.Builder().build();
        ResourceState bob = new DefaultResourceState("bob");
        bob.putProperty("name", "Bob McWhirter");

        ResourceState result = this.client.create(requestContext, "/db/people", bob);

        assertThat(result).isNotNull();
        assertThat(result.getProperty("name")).isEqualTo("Bob McWhirter");

        ResourceState foundBob = this.client.read(requestContext, "/db/people/bob");
        assertThat(foundBob).isNotNull();
        assertThat(foundBob.getProperty("name")).isEqualTo("Bob McWhirter");

        bob = new DefaultResourceState("bob");
        bob.putProperty("name", "Robert McWhirter");

        result = this.client.update(requestContext, "/db/people/bob", bob);
        assertThat(result).isNotNull();
        assertThat(result.getProperty("name")).isEqualTo("Robert McWhirter");

        foundBob = this.client.read(requestContext, "/db/people/bob");
        assertThat(foundBob).isNotNull();
        assertThat(foundBob.getProperty("name")).isEqualTo("Robert McWhirter");
    }

    @Test(timeout = 10000)
    public void testReadFromResource() throws Exception {

        RequestContext requestContext = new RequestContext.Builder().build();
        ResourceState bob = new DefaultResourceState("proxybob");
        bob.putProperty("name", "Bob McWhirter");

        ResourceState createdBob = this.client.create(requestContext, "/db/people", bob);

        assertThat(createdBob).isNotNull();

        RootResource proxy = new ProxyResource();
        system.directDeployer().deploy(proxy);

        ResourceState result = this.client.read(requestContext, "/proxy");
        assertThat(result.getPropertyNames()).contains("name");
        assertThat(result.getProperty("name")).isEqualTo("Bob McWhirter");

        system.directDeployer().undeploy(proxy);
    }



    private static class ProxyResource implements RootResource {

        private Client client;

        @Override
        public void initialize(ResourceContext context) throws InitializationException {
            this.client = context.client();
        }

        @Override
        public String id() {
            return "proxy";
        }

        @Override
        public void readProperties(RequestContext ctx, PropertySink sink) throws Exception {
            RequestContext requestContext = new RequestContext.Builder().build();
            client.read(requestContext, "/db/people/proxybob", (r) -> {
                ResourceState result = r.state();
                for (String n : result.getPropertyNames()) {
                    sink.accept(n, result.getProperty(n));
                }
                try {
                    sink.close();
                } catch (Exception e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
            });
        }
    }

    @Test(timeout = 10000)
    public void testReadFromBlockingResource() throws Exception {

        RequestContext requestContext = new RequestContext.Builder().build();
        ResourceState bob = new DefaultResourceState("blockingproxybob");
        bob.putProperty("name", "Bob McWhirter");

        ResourceState createdBob = this.client.create(requestContext, "/db/people", bob);

        assertThat(createdBob).isNotNull();

        RootResource proxy = new BlockingProxyResource();
        system.directDeployer().deploy(proxy);

        ResourceState result = this.client.read(requestContext, "/proxy");
        assertThat(result.getPropertyNames()).contains("name");
        assertThat(result.getProperty("name")).isEqualTo("Bob McWhirter");

        system.directDeployer().undeploy(proxy);
    }

    private static class BlockingProxyResource implements RootResource, BlockingResource {

        private Client client;

        @Override
        public void initialize(ResourceContext context) throws InitializationException {
            this.client = context.client();
        }

        @Override
        public String id() {
            return "proxy";
        }

        @Override
        public void readProperties(RequestContext ctx, PropertySink sink) throws Exception {
            RequestContext requestContext = new RequestContext.Builder().build();
            ResourceState result = client.read(requestContext, "/db/people/blockingproxybob");
            for (String n : result.getPropertyNames()) {
                sink.accept(n, result.getProperty(n));
            }
            try {
                sink.close();
            } catch (Exception e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }
    }
    /*

    @Test
    public void testFetch() throws Throwable {

        RequestContext requestContext = new RequestContext.Builder().build();
        ResourceState bob = new DefaultResourceState("bob");
        bob.putProperty("name", "Bob McWhirter");

        ResourceState result = this.client.create(requestContext, "/db/people", bob);

        Resource bobResource = this.client.fetch("/db/people/bob");

        assertThat(bobResource).isNotNull();
        assertThat(bobResource.id()).isEqualTo("bob");

    }
    */

}
