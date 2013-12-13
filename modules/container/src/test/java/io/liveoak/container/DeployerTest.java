package io.liveoak.container;

import io.liveoak.common.codec.DefaultResourceState;
import io.liveoak.spi.Container;
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
public class DeployerTest {

    private LiveOakSystem system;
    private Container container;
    private Client client;

    @Before
    public void setUp() throws Exception {
        this.system = LiveOakFactory.create();
        this.container = this.system.container();
        this.client = this.system.client();
    }

    @After
    public void tearDown() throws Exception {
        this.system.stop();
    }

    @Test
    public void testDeployResource() throws Exception {

        RequestContext context = new RequestContext.Builder().build();
        ResourceState state = new DefaultResourceState();

        state.id("memory");
        state.putProperty("type", "classpath");
        state.putProperty("class-name", InMemoryDBResource.class.getName());

        ResourceState result = this.client.create(context, "/", state);

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo("memory");

        // attempt to use it

        state = new DefaultResourceState();
        state.putProperty("name", "bob");

        result = this.client.create(context, "/memory", state);

        assertThat(result).isNotNull();
        assertThat(result.getProperty("name")).isEqualTo("bob");

        result = this.client.read(context, "/memory/" + result.id());

        assertThat(result).isNotNull();
        assertThat(result.getProperty("name")).isEqualTo("bob");
    }

    @Test
    public void testUndeployResource() throws Exception {

        RequestContext context = new RequestContext.Builder().build();
        ResourceState state = new DefaultResourceState();

        state.id("memory");
        state.putProperty("type", "classpath");
        state.putProperty("class-name", InMemoryDBResource.class.getName());

        ResourceState result = this.client.create(context, "/", state);

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo("memory");

        result = this.client.delete(context, "/memory");

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo("memory");

        try {
            this.client.read(context, "/memory");
            fail( "Should have deleted the resource /memory" );
        } catch (ResourceNotFoundException e) {
            // expected and correct
        }

    }
}
