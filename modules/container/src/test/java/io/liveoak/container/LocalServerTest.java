package io.liveoak.container;

import io.liveoak.spi.RequestContext;
import io.liveoak.spi.client.Client;
import io.liveoak.spi.state.ResourceState;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;

/**
 * @author Bob McWhirter
 */
public class LocalServerTest {

    private LiveOakSystem system;

    @Before
    public void setUp() throws Exception {
        this.system = LiveOakFactory.create();
        this.system.awaitStability();
    }

    @After
    public void tearDown() throws Exception {
        this.system.stop();
    }

    @Test
    public void testClient() throws Exception {

        Client client = this.system.client();

        RequestContext requestContext = new RequestContext.Builder().build();

        ResourceState state = client.read(requestContext, "/");

        assertThat(state).isNotNull();
        assertThat(state.id()).isEmpty();
        assertThat(state.uri().toString()).isEqualTo("/");

    }
}
