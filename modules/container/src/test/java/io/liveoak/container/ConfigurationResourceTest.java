package io.liveoak.container;

import io.liveoak.container.codec.DefaultResourceState;
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

    private static final String CONFIG_PARAM = ";config";
    private static final String ROOT_WITH_CONFIG = "/" + CONFIG_PARAM;
    private static final String FIRST_KEY = "path1";
    private static final String FIRST_VALUE = "firstPath";
    private static final String SECOND_KEY = "other";
    private static final String SECOND_VALUE = "secondPath";
    private static final String RESOURCE = "memory";
    private static final String RESOURCE_WITH_CONFIG = "/" + RESOURCE + CONFIG_PARAM;

    private DefaultContainer container;
    private DirectConnector connector;

    @Before
    public void setUp() throws Exception {
        this.container = new DefaultContainer();
        InMemoryConfigResource resource = new InMemoryConfigResource(RESOURCE);
        DefaultResourceState state = new DefaultResourceState();
        state.putProperty(FIRST_KEY, FIRST_VALUE);
        state.putProperty(SECOND_KEY, SECOND_VALUE);
        this.container.registerResource(resource, state);

        this.connector = this.container.directConnector();
    }

    @Test
    public void testFetchConfiguration() throws Exception {
        Resource configResource = this.connector.fetch(ROOT_WITH_CONFIG);
        assertThat(configResource).isNotNull();
        assertThat(configResource).isInstanceOf(ContainerConfigurationResource.class);

    }

    @Test
    public void testReadConfiguration() throws Exception {
        RequestContext context = new RequestContext.Builder().build();

        ResourceState configState = this.connector.read(context, ROOT_WITH_CONFIG);

        assertThat(configState).isNotNull();
        assertThat(configState.id()).isEqualTo(CONFIG_PARAM);
        assertThat(configState.uri().toString()).isEqualTo(ROOT_WITH_CONFIG);

    }

    @Test
    public void testConfigPropertyString() throws Exception {
        RequestContext context = new RequestContext.Builder().build();

        ResourceState configState = this.connector.read(context, RESOURCE_WITH_CONFIG);

        assertThat(configState).isNotNull();
        assertThat(configState.id()).isEqualTo(CONFIG_PARAM);
        assertThat(configState.uri().toString()).isEqualTo(RESOURCE_WITH_CONFIG);

        Object value = configState.getProperty(FIRST_KEY);
        assertThat(value).isNotNull();
        assertThat(value).isInstanceOf(String.class);
        assertThat(value).toString().equals(FIRST_VALUE);

        value = configState.getProperty(SECOND_KEY);
        assertThat(value).isNotNull();
        assertThat(value).isInstanceOf(String.class);
        assertThat(value).toString().equals(SECOND_VALUE);
    }
}
