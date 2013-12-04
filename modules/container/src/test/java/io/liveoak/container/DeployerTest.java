package io.liveoak.container;

import io.liveoak.container.codec.DefaultResourceState;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.ResourceNotFoundException;
import io.liveoak.spi.state.ResourceState;
import org.junit.Before;
import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;
import static org.junit.Assert.*;

/**
 * @author Bob McWhirter
 */
public class DeployerTest {

    private DefaultContainer container;
    private DirectConnector connector;

    @Before
    public void setUp() throws Exception {
        this.container = new DefaultContainer();
        this.connector = new DirectConnector(this.container);
    }

    @Test
    public void testDeployResource() throws Exception {

        RequestContext context = new RequestContext.Builder().build();
        ResourceState state = new DefaultResourceState();

        state.id("memory");
        state.putProperty("type", "classpath");
        state.putProperty("class-name", InMemoryDBResource.class.getName());

        ResourceState result = this.connector.create(context, "/", state);

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo("memory");

        // attempt to use it

        state = new DefaultResourceState();
        state.putProperty("name", "bob");

        result = this.connector.create(context, "/memory", state);

        assertThat(result).isNotNull();
        assertThat(result.getProperty("name")).isEqualTo("bob");

        result = this.connector.read(context, "/memory/" + result.id());

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

        ResourceState result = this.connector.create(context, "/", state);

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo("memory");

        result = this.connector.delete(context, "/memory");

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo("memory");

        try {
            this.connector.read(context, "/memory");
            fail( "Should have deleted the resource /memory" );
        } catch (ResourceNotFoundException e) {
            // expected and correct
        }

    }
}
