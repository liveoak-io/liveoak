package io.liveoak.container;

import java.io.File;
import java.net.URL;

import io.liveoak.common.codec.DefaultResourceState;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.client.Client;
import io.liveoak.spi.state.ResourceState;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

import static org.fest.assertions.Assertions.assertThat;

/**
 * @author Bob McWhirter
 */
public class ConfigurationResourceTest {

    private LiveOakSystem system;
    private Client client;

    private static final String CONFIG_PARAM = ";config";
    private static final String ROOT_WITH_CONFIG = "/" + CONFIG_PARAM;
    private static final String FIRST_KEY = "path1";
    private static final String FIRST_VALUE = "firstPath";
    private static final String SECOND_KEY = "other";
    private static final String SECOND_VALUE = "secondPath";
    private static final String RESOURCE = "memory";
    private static final String RESOURCE_WITH_CONFIG = "/" + RESOURCE + CONFIG_PARAM;


    private File projectRoot;

    @Before
    public void setupUserDir() {
        String name = getClass().getName().replace(".", "/") + ".class";
        URL resource = getClass().getClassLoader().getResource(name);

        if (resource != null) {
            File current = new File(resource.getFile());

            while (current.exists()) {
                if (current.isDirectory()) {
                    if (new File(current, "pom.xml").exists()) {
                        this.projectRoot = current;
                        break;
                    }
                }

                current = current.getParentFile();
            }
        }
    }

    @Before
    public void setUp() throws Exception {
        File etc = new File(this.projectRoot, "target/etc");
        File resources = new File(etc, "resources");
        if (resources.exists()) {
            File[] children = resources.listFiles();
            for (File child : children) {
                child.delete();
            }
        }
        this.system = LiveOakFactory.create(new File(this.projectRoot, "target/etc"));
        this.client = this.system.client();
        InMemoryConfigResource resource = new InMemoryConfigResource(RESOURCE);
        DefaultResourceState state = new DefaultResourceState();
        state.putProperty(FIRST_KEY, FIRST_VALUE);
        state.putProperty(SECOND_KEY, SECOND_VALUE);
        this.system.directDeployer().deploy(resource, state);
    }

    @After
    public void tearDown() {
        this.system.stop();
    }

    /*
    @Test
    public void testFetchConfiguration() throws Exception {
        Resource configResource = this.client.fetch(ROOT_WITH_CONFIG);
        assertThat(configResource).isNotNull();
        assertThat(configResource).isInstanceOf(ContainerConfigurationResource.class);

    }
    */

    /*
    @Test
    public void testReadConfiguration() throws Exception {
        RequestContext context = new RequestContext.Builder().build();

        ResourceState configState = this.client.read(context, ROOT_WITH_CONFIG);

        assertThat(configState).isNotNull();
        assertThat(configState.id()).isEqualTo(CONFIG_PARAM);
        assertThat(configState.uri().toString()).isEqualTo(ROOT_WITH_CONFIG);

    }
    */

    @Test
    public void testConfigPropertyString() throws Exception {
        RequestContext context = new RequestContext.Builder().build();

        ResourceState configState = this.client.read(context, RESOURCE_WITH_CONFIG);

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

    @Test
    public void testConfigPropertyNotFound() throws Exception {
        InMemoryConfigResource resource = new InMemoryConfigResource(RESOURCE + 3);
        DefaultResourceState state = new DefaultResourceState();
        state.putProperty(SECOND_KEY, SECOND_VALUE);
        try {
            this.system.directDeployer().deploy(resource, state);
            fail("Should throw due to missing config for 'path1'");
        } catch (Exception e) {
            // expected and correct
        }
    }

    @Test
    public void testConfigPropertyConverter() throws Exception {
        InMemoryConfigResourceWithConverter resource = new InMemoryConfigResourceWithConverter(RESOURCE + 5);
        DefaultResourceState state = new DefaultResourceState();
        state.putProperty("file", this.projectRoot.getAbsolutePath());
        state.putProperty("firstValue", "firstValue");
        state.putProperty("secondValue", "another");
        this.system.directDeployer().deploy(resource, state);

        RequestContext context = new RequestContext.Builder().build();

        ResourceState configState = this.client.read(context, "/" + RESOURCE + 5 + CONFIG_PARAM);

        assertThat(configState).isNotNull();

        Object value = configState.getProperty("file");
        assertThat(value).isNotNull();
        assertThat(value).isInstanceOf(String.class);
        assertThat(value).toString().equals(this.projectRoot.getAbsolutePath());

        value = configState.getProperty("firstValue");
        assertThat(value).isNotNull();
        assertThat(value).isInstanceOf(String.class);
        assertThat(value).toString().equals("firstValue");

        value = configState.getProperty("secondValue");
        assertThat(value).isNotNull();
        assertThat(value).isInstanceOf(String.class);
        assertThat(value).toString().equals("another");
    }
}
