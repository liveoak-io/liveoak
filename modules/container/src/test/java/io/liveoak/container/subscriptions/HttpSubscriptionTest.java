package io.liveoak.container.subscriptions;

import io.liveoak.container.*;
import io.liveoak.common.codec.DefaultResourceState;
import io.liveoak.container.tenancy.InternalApplication;
import io.liveoak.container.tenancy.InternalOrganization;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.ResourceNotFoundException;
import io.liveoak.spi.client.Client;
import io.liveoak.spi.state.ResourceState;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;
import static org.junit.Assert.*;

/**
 * @author Bob McWhirter
 */
public class HttpSubscriptionTest {

    protected LiveOakSystem system;
    protected Client client;
    private InternalOrganization organization;
    private InternalApplication application;

    @Before
    public void setUp() throws Exception {
        this.system = LiveOakFactory.create();
        this.organization = this.system.organizationRegistry().createOrganization( "testOrg", "Test Organization" );
        this.application = this.organization.createApplication( "testApp", "Test Application" );

        this.system.extensionInstaller().load("memory", new InMemoryDBExtension());
        this.application.extend("memory");
        this.system.awaitStability();

        InMemoryDBResource resource = (InMemoryDBResource) this.system.service( InMemoryDBExtension.resource("testOrg", "testApp", "memory") );
        resource.addMember(new InMemoryCollectionResource(resource, "data"));
        resource.addMember(new InMemoryCollectionResource(resource, "notifications"));

        this.client = this.system.client();
    }

    @After
    public void tearDown() throws Exception {
        this.system.stop();
    }

    @Test
    public void testHttpSubscription() throws Exception {

        RequestContext requestContext = new RequestContext.Builder().build();

        // Create a subscription

        DefaultResourceState subscriptionState = new DefaultResourceState();
        subscriptionState.putProperty("path", "/testOrg/testApp/memory/data/*");
        subscriptionState.putProperty("destination", "http://localhost:8080/testOrg/testApp/memory/notifications/");
        ResourceState createdSubscription = this.client.create(requestContext, "/testOrg/testApp/subscriptions", subscriptionState);

        assertThat(createdSubscription).isNotNull();
        assertThat(createdSubscription.getProperty("path")).isEqualTo("/testOrg/testApp/memory/data/*");
        assertThat(createdSubscription.getProperty("destination")).isEqualTo("http://localhost:8080/testOrg/testApp/memory/notifications/");

        // Create an item that is subscribed to

        DefaultResourceState bobState = new DefaultResourceState();
        bobState.putProperty("name", "Bob McWhirter");
        ResourceState createdBob = this.client.create(requestContext, "/testOrg/testApp/memory/data", bobState);

        String bobId = createdBob.id();
        assertThat(bobId).isNotEmpty();

        // Give subscription time to deliver

        Thread.sleep(1000);
        assertThat(createdBob.uri().toString()).isEqualTo("/testOrg/testApp/memory/data/" + bobId);

        // Check that subscription fired, creating target

        ResourceState notifiedBob = this.client.read(requestContext, "/testOrg/testApp/memory/notifications/" + bobId);

        assertThat(notifiedBob).isNotNull();
        assertThat(notifiedBob.id()).isEqualTo(bobId);
        assertThat(notifiedBob.uri().toString()).isEqualTo("/testOrg/testApp/memory/notifications/" + bobId);
        assertThat(notifiedBob.getPropertyNames()).hasSize(1);
        assertThat(notifiedBob.getProperty("name")).isEqualTo("Bob McWhirter");

        // Delete a subscribed thing

        ResourceState deletedBob = this.client.delete(requestContext, createdBob.uri().toString());

        // Give it time to propagate

        Thread.sleep(1000);

        // ensure delete was notified to subscriber

        try {
            this.client.read(requestContext, notifiedBob.uri().toString());
            fail("Should have thrown ResourceNotFoundException");
        } catch (ResourceNotFoundException e) {
            // expected and correct
        }

        // Delete the subscription

        ResourceState deletedSubscription = this.client.delete(requestContext, createdSubscription.uri().toString());
        assertThat(deletedSubscription).isNotNull();

        // Ensure that further notifications do not occur.

        DefaultResourceState kenState = new DefaultResourceState();
        kenState.putProperty("name", "Ken Finnigan");
        ResourceState createdKen = this.client.create(requestContext, "/testOrg/testApp/memory/data", kenState);

        String kenId = createdKen.id();
        assertThat(kenId).isNotEmpty();

        Thread.sleep(2000);

        try {
            this.client.read(requestContext, "/testOrg/testApp/memory/notifications/" + kenId);
            fail("should have thrown ResourceNotFoundException");
        } catch (ResourceNotFoundException e) {
            // expected and corret
        }

    }
}

