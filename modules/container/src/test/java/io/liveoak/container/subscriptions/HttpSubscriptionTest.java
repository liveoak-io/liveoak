package io.liveoak.container.subscriptions;

import io.liveoak.container.*;
import io.liveoak.container.codec.DefaultResourceState;
import io.liveoak.container.server.UnsecureServer;
import io.liveoak.spi.Container;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.ResourceNotFoundException;
import io.liveoak.spi.container.DirectConnector;
import io.liveoak.spi.container.Server;
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
    protected DirectConnector connector;

    @Before
    public void setUp() throws Exception {
        this.system = LiveOakFactory.create();

        InMemoryDBResource resource = new InMemoryDBResource("memory");
        resource.addMember(new InMemoryCollectionResource(resource, "data"));
        resource.addMember(new InMemoryCollectionResource(resource, "notifications"));

        this.system.directDeployer().deploy( resource );

        this.connector = this.system.directConnector();
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
        subscriptionState.putProperty("path", "/memory/data/*");
        subscriptionState.putProperty("destination", "http://localhost:8080/memory/notifications/");
        ResourceState createdSubscription = this.connector.create(requestContext, "/subscriptions", subscriptionState);

        assertThat(createdSubscription).isNotNull();
        assertThat(createdSubscription.getProperty("path")).isEqualTo("/memory/data/*");
        assertThat(createdSubscription.getProperty("destination")).isEqualTo("http://localhost:8080/memory/notifications/");

        // Create an item that is subscribed to

        DefaultResourceState bobState = new DefaultResourceState();
        bobState.putProperty("name", "Bob McWhirter");
        ResourceState createdBob = this.connector.create(requestContext, "/memory/data", bobState);

        String bobId = createdBob.id();
        assertThat(bobId).isNotEmpty();

        // Give subscription time to deliver

        Thread.sleep(1000);
        assertThat(createdBob.uri().toString()).isEqualTo("/memory/data/" + bobId);

        // Check that subscription fired, creating target

        ResourceState notifiedBob = this.connector.read(requestContext, "/memory/notifications/" + bobId);

        assertThat(notifiedBob).isNotNull();
        assertThat(notifiedBob.id()).isEqualTo(bobId);
        assertThat(notifiedBob.uri().toString()).isEqualTo("/memory/notifications/" + bobId);
        assertThat(notifiedBob.getPropertyNames()).hasSize(1);
        assertThat(notifiedBob.getProperty("name")).isEqualTo("Bob McWhirter");

        // Delete a subscribed thing

        ResourceState deletedBob = this.connector.delete(requestContext, createdBob.uri().toString());

        // Give it time to propagate

        Thread.sleep(1000);

        // ensure delete was notified to subscriber

        try {
            this.connector.read(requestContext, notifiedBob.uri().toString());
            fail("Should have thrown ResourceNotFoundException");
        } catch (ResourceNotFoundException e) {
            // expected and correct
        }

        // Delete the subscription

        ResourceState deletedSubscription = this.connector.delete(requestContext, createdSubscription.uri().toString());
        assertThat(deletedSubscription).isNotNull();

        // Ensure that further notifications do not occur.

        DefaultResourceState kenState = new DefaultResourceState();
        kenState.putProperty("name", "Ken Finnigan");
        ResourceState createdKen = this.connector.create(requestContext, "/memory/data", kenState);

        String kenId = createdKen.id();
        assertThat(kenId).isNotEmpty();

        Thread.sleep(2000);

        try {
            this.connector.read(requestContext, "/memory/notifications/" + kenId);
            fail("should have thrown ResourceNotFoundException");
        } catch (ResourceNotFoundException e) {
            // expected and corret
        }

    }
}

