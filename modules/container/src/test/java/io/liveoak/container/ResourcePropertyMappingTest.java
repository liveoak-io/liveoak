package io.liveoak.container;

import java.io.File;
import java.net.URL;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.liveoak.container.resource.mapping.InMemoryConfigConverterExtension;
import io.liveoak.container.resource.mapping.InMemoryConfigExtension;
import io.liveoak.container.resource.mapping.InMemoryConfigMultiValueExtension;
import io.liveoak.container.resource.mapping.InMemoryConfigTypesExtension;
import io.liveoak.container.tenancy.InternalApplication;
import io.liveoak.container.zero.extension.ZeroExtension;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.client.Client;
import io.liveoak.spi.state.ResourceState;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;
import static org.junit.Assert.fail;

/**
 * @author Bob McWhirter
 * @author Ken Finnigan
 */
public class ResourcePropertyMappingTest extends AbstractContainerTest {

    private static Client client;
    private static InternalApplication application;

    private static final String ADMIN_PATH = "/" + ZeroExtension.APPLICATION_ID + "/applications/testApp/resources/";
    private static final String CONFIG_RESOURCE_ID = "config";
    private static final String CONFIG_CONVERTER_RESOURCE_ID = "config5";
    private static final String CONFIG_MULTI_RESOURCE_ID = "config6";
    private static final String CONFIG_TYPE_RESOURCE_ID = "config7";
    private static final String FIRST_KEY = "path1";
    private static final String FIRST_VALUE = "firstPath";
    private static final String SECOND_KEY = "other";
    private static final String SECOND_VALUE = "secondPath";

    private static File projectRoot;

    @BeforeClass
    public static void setupUserDir() {
        String name = ResourcePropertyMappingTest.class.getName().replace(".", "/") + ".class";
        URL resource = ResourcePropertyMappingTest.class.getClassLoader().getResource(name);

        if (resource != null) {
            File current = new File(resource.getFile());

            while (current.exists()) {
                if (current.isDirectory()) {
                    if (new File(current, "pom.xml").exists()) {
                        projectRoot = current;
                        break;
                    }
                }

                current = current.getParentFile();
            }
        }
    }

    @BeforeClass
    public static void setUp() throws Exception {
        system = LiveOakFactory.create();
        setupMocks();
        client = system.client();
        system.extensionInstaller().load("config", new InMemoryConfigExtension());

        awaitStability();

        system.applicationRegistry().createApplication(ZeroExtension.APPLICATION_ID, ZeroExtension.APPLICATION_NAME);
        application = system.applicationRegistry().createApplication("testApp", "Test Application");

        awaitStability();

        ObjectNode configNode = JsonNodeFactory.instance.objectNode();
        configNode.put(FIRST_KEY, FIRST_VALUE);
        configNode.put(SECOND_KEY, SECOND_VALUE);
        application.extend("config", CONFIG_RESOURCE_ID, configNode);
        awaitStability();
    }

    @AfterClass
    public static void shutdown() {
        system.stop();
    }

    @Test
    public void readConfiguration() throws Exception {
        ResourceState configState = client.read(new RequestContext.Builder().build(), ADMIN_PATH + CONFIG_RESOURCE_ID);

        assertThat(configState).isNotNull();
        assertThat(configState.id()).isEqualTo(CONFIG_RESOURCE_ID);
        assertThat(configState.uri().toString()).isEqualTo(ADMIN_PATH + CONFIG_RESOURCE_ID);
    }

    @Test
    public void configPropertyString() throws Exception {
        ResourceState configState = client.read(new RequestContext.Builder().build(), ADMIN_PATH + CONFIG_RESOURCE_ID);

        assertThat(configState).isNotNull();
        assertThat(configState.id()).isEqualTo(CONFIG_RESOURCE_ID);
        assertThat(configState.uri().toString()).isEqualTo(ADMIN_PATH + CONFIG_RESOURCE_ID);

        Object value = configState.getProperty(FIRST_KEY);
        assertThat(value).isNotNull();
        assertThat(value).isInstanceOf(String.class);
        assertThat(value.toString()).isEqualTo(FIRST_VALUE);

        value = configState.getProperty(SECOND_KEY);
        assertThat(value).isNotNull();
        assertThat(value).isInstanceOf(String.class);
        assertThat(value.toString()).isEqualTo(SECOND_VALUE);
    }

    @Test
    public void configPropertyNotFound() throws Exception {
        try {
            ObjectNode configNode = JsonNodeFactory.instance.objectNode();
            configNode.put(SECOND_KEY, SECOND_VALUE);
            application.extend("config", "config3", configNode);
            fail("Should throw due to missing config for 'path1'");
        } catch (Exception e) {
            // expected and correct
        }
    }

    @Test
    public void configPropertyConverter() throws Exception {
        system.extensionInstaller().load("config-converter", new InMemoryConfigConverterExtension());

        ObjectNode configNode = JsonNodeFactory.instance.objectNode();
        configNode.put("file", projectRoot.getAbsolutePath());
        application.extend("config-converter", CONFIG_CONVERTER_RESOURCE_ID, configNode);
        awaitStability();

        ResourceState configState = client.read(new RequestContext.Builder().build(), ADMIN_PATH + CONFIG_CONVERTER_RESOURCE_ID);

        assertThat(configState).isNotNull();

        Object value = configState.getProperty("file");
        assertThat(value).isNotNull();
        assertThat(value).isInstanceOf(String.class);
        assertThat(value.toString()).isEqualTo(projectRoot.getAbsolutePath());
    }

    @Test
    public void configMultiPropertyConversion() throws Exception {
        system.extensionInstaller().load("config-multi", new InMemoryConfigMultiValueExtension());

        ObjectNode configNode = JsonNodeFactory.instance.objectNode();
        configNode.put("firstValue", "firstValue");
        configNode.put("secondValue", "another");
        application.extend("config-multi", CONFIG_MULTI_RESOURCE_ID, configNode);
        awaitStability();

        ResourceState configState = client.read(new RequestContext.Builder().build(), ADMIN_PATH + CONFIG_MULTI_RESOURCE_ID);

        assertThat(configState).isNotNull();

        Object value = configState.getProperty("firstValue");
        assertThat(value).isNotNull();
        assertThat(value).isInstanceOf(String.class);
        assertThat(value.toString()).isEqualTo("firstValue");

        value = configState.getProperty("secondValue");
        assertThat(value).isNotNull();
        assertThat(value).isInstanceOf(String.class);
        assertThat(value.toString()).isEqualTo("another");
    }

    @Test
    public void configPropertyTypeConversion() throws Exception {
        system.extensionInstaller().load("config-types", new InMemoryConfigTypesExtension());

        ObjectNode configNode = JsonNodeFactory.instance.objectNode();
        configNode.put("file", projectRoot.getAbsolutePath());
        configNode.put("flag", true);
        configNode.put("url", "http://liveoak.io");
        configNode.put("uri", "urn:isbn:0451450523");
        configNode.put("dbl", new Double("4.6"));
        configNode.put("integer", new Integer("35"));
        application.extend("config-types", CONFIG_TYPE_RESOURCE_ID, configNode);
        awaitStability();

        ResourceState configState = client.read(new RequestContext.Builder().build(), ADMIN_PATH + CONFIG_TYPE_RESOURCE_ID);
        assertThat(configState).isNotNull();

        Object value = configState.getProperty("file");
        assertThat(value).isNotNull();
        assertThat(value).isInstanceOf(String.class);
        assertThat(value.toString()).isEqualTo(projectRoot.getAbsolutePath());

        value = configState.getProperty("flag");
        assertThat(value).isNotNull();
        assertThat(value).isInstanceOf(Boolean.class);
        assertThat(value).isEqualTo(Boolean.TRUE);

        value = configState.getProperty("url");
        assertThat(value).isNotNull();
        assertThat(value).isInstanceOf(String.class);
        assertThat(value.toString()).isEqualTo("http://liveoak.io");

        value = configState.getProperty("uri");
        assertThat(value).isNotNull();
        assertThat(value).isInstanceOf(String.class);
        assertThat(value.toString()).isEqualTo("urn:isbn:0451450523");

        value = configState.getProperty("dbl");
        assertThat(value).isNotNull();
        assertThat(value).isInstanceOf(Double.class);
        assertThat((double) value).isEqualTo(4.6);

        value = configState.getProperty("integer");
        assertThat(value).isNotNull();
        assertThat(value).isInstanceOf(Integer.class);
        assertThat((int) value).isEqualTo(35);
    }
}
