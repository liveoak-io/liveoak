package io.liveoak.container;

import io.liveoak.container.resource.ContainerConfigurationResource;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.resource.async.Resource;
import io.liveoak.spi.state.ResourceState;
import org.junit.Before;
import org.junit.Test;
import static org.fest.assertions.Assertions.assertThat;

/**
 * @author Bob McWhirter
 */
public class ConfigurationResourceTest {

    private DefaultContainer container;
    private DirectConnector connector;

    @Before
    public void setUp() throws Exception {
        this.container = new DefaultContainer();
        this.connector = this.container.directConnector();
    }

    @Test
    public void testFetchConfiguration() throws Exception {
        Resource configResource = this.connector.fetch( "/;config" );
        assertThat( configResource ).isNotNull();
        assertThat( configResource ).isInstanceOf(ContainerConfigurationResource.class );

    }

    @Test
    public void testReadConfiguration() throws Exception {
        RequestContext context = new RequestContext.Builder().build();

        ResourceState configState = this.connector.read( context, "/;config" );

        assertThat( configState ).isNotNull();
        assertThat( configState.id() ).isEqualTo( ";config" );
        assertThat( configState.uri().toString() ).isEqualTo( "/;config" );


    }
}
