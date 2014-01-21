/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.container;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import io.liveoak.common.codec.DefaultResourceState;
import io.liveoak.container.tenancy.InternalApplication;
import io.liveoak.container.tenancy.InternalApplicationExtension;
import io.liveoak.container.tenancy.InternalOrganization;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.ReturnFields;
import io.liveoak.spi.client.Client;
import io.liveoak.spi.state.ResourceState;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.fest.assertions.Assertions.assertThat;

/**
 * @author Bob McWhirter
 */
public class ClientTest {

    private LiveOakSystem system;
    private Client client;
    private InternalOrganization organization;
    private InternalApplication application;


    @Before
    public void setUp() throws Exception {
        this.system = LiveOakFactory.create();
        this.client = this.system.client();

        this.organization = this.system.organizationRegistry().createOrganization( "testOrg", "Test Organization" );
        this.application = this.organization.createApplication( "testApp", "Test Application" );

        this.system.extensionInstaller().load( "db", new InMemoryDBExtension() );
        this.application.extend( "db" );
        this.system.awaitStability();

        InMemoryDBResource db = (InMemoryDBResource) this.system.service( InMemoryDBExtension.resource( "testOrg", "testApp", "db") );
        db.addMember(new InMemoryCollectionResource(db, "people"));
        db.addMember(new InMemoryCollectionResource(db, "dogs"));

    }

    @After
    public void shutdown() throws Exception {
        this.system.stop();
    }

    @Test
    public void testRead() throws Throwable {

        ReturnFields fields = new ReturnFieldsImpl("*,members(*,members(*))");

        RequestContext requestContext = new RequestContext.Builder().returnFields(fields).build();
        ResourceState result = this.client.read(requestContext, "/testOrg/testApp");
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

        ResourceState result = this.client.create(requestContext, "/testOrg/testApp/db/people", bob);

        assertThat(result).isNotNull();
        assertThat(result.getProperty("name")).isEqualTo("Bob McWhirter");

        ResourceState people = this.client.read(requestContext, "/testOrg/testApp/db/people");

        assertThat(people).isNotNull();

        System.err.println(people);

        ResourceState foundBob = people.members().stream().filter((e) -> e.id().equals("bob")).findFirst().get();
        assertThat(foundBob).isNotNull();
        assertThat(foundBob.id()).isEqualTo("bob");
        assertThat(foundBob.getPropertyNames()).hasSize(0);

        foundBob = this.client.read(requestContext, "/testOrg/testApp/db/people/bob");

        assertThat(foundBob).isNotNull();
        assertThat(foundBob.getProperty("name")).isEqualTo("Bob McWhirter");
    }

    @Test
    public void testUpdate() throws Throwable {
        RequestContext requestContext = new RequestContext.Builder().build();
        ResourceState bob = new DefaultResourceState("bob");
        bob.putProperty("name", "Bob McWhirter");

        ResourceState result = this.client.create(requestContext, "/testOrg/testApp/db/people", bob);

        assertThat(result).isNotNull();
        assertThat(result.getProperty("name")).isEqualTo("Bob McWhirter");

        ResourceState foundBob = this.client.read(requestContext, "/testOrg/testApp/db/people/bob");
        assertThat(foundBob).isNotNull();
        assertThat(foundBob.getProperty("name")).isEqualTo("Bob McWhirter");

        bob = new DefaultResourceState("bob");
        bob.putProperty("name", "Robert McWhirter");

        result = this.client.update(requestContext, "/testOrg/testApp/db/people/bob", bob);
        assertThat(result).isNotNull();
        assertThat(result.getProperty("name")).isEqualTo("Robert McWhirter");

        foundBob = this.client.read(requestContext, "/testOrg/testApp/db/people/bob");
        assertThat(foundBob).isNotNull();
        assertThat(foundBob.getProperty("name")).isEqualTo("Robert McWhirter");
    }

    @Test(timeout = 10000)
    public void testReadFromResource() throws Exception {

        RequestContext requestContext = new RequestContext.Builder().build();
        ResourceState bob = new DefaultResourceState("proxybob");
        bob.putProperty("name", "Bob McWhirter");

        ResourceState createdBob = this.client.create(requestContext, "/testOrg/testApp/db/people", bob);

        assertThat(createdBob).isNotNull();

        this.system.extensionInstaller().load( "proxy", new ProxyExtension() );
        InternalApplicationExtension ext = this.application.extend("proxy", JsonNodeFactory.instance.objectNode().put("blocking", false));
        this.system.awaitStability();

        ResourceState result = this.client.read(requestContext, "/testOrg/testApp/proxy");
        assertThat(result.getPropertyNames()).contains("name");
        assertThat(result.getProperty("name")).isEqualTo("Bob McWhirter");

        ext.remove();
    }


    @Test(timeout = 10000)
    public void testReadFromBlockingResource() throws Exception {

        RequestContext requestContext = new RequestContext.Builder().build();
        ResourceState bob = new DefaultResourceState("blockingproxybob");
        bob.putProperty("name", "Bob McWhirter");

        ResourceState createdBob = this.client.create(requestContext, "/testOrg/testApp/db/people", bob);

        assertThat(createdBob).isNotNull();

        System.err.println( "A" );
        this.system.extensionInstaller().load( "proxy", new ProxyExtension() );
        InternalApplicationExtension ext = this.application.extend("proxy", JsonNodeFactory.instance.objectNode().put("blocking", true));
        this.system.awaitStability();
        System.err.println( "B" );

        ResourceState result = this.client.read(requestContext, "/testOrg/testApp/proxy");
        System.err.println( "C" );
        assertThat(result.getPropertyNames()).contains("name");
        assertThat(result.getProperty("name")).isEqualTo("Bob McWhirter");

        ext.remove();
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
