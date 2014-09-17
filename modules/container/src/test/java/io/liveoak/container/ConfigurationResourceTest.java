package io.liveoak.container;

import java.io.File;
import java.net.URL;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.liveoak.container.tenancy.InternalApplication;
import io.liveoak.container.zero.extension.ZeroExtension;
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
 * @author Ken Finnigan
 */
public class ConfigurationResourceTest {

    private LiveOakSystem system;
    private Client client;
    private InternalApplication application;

    private static final String ADMIN_PATH = "/" + ZeroExtension.APPLICATION_ID + "/applications/testApp/resources/";
    private static final String CONFIG_RESOURCE_ID = "config";
    private static final String CONFIG_CONVERTER_RESOURCE_ID = "config5";
    private static final String CONFIG_MULTI_RESOURCE_ID = "config6";
    private static final String CONFIG_TYPE_RESOURCE_ID = "config7";
    private static final String FIRST_KEY = "path1";
    private static final String FIRST_VALUE = "firstPath";
    private static final String SECOND_KEY = "other";
    private static final String SECOND_VALUE = "secondPath";

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
        this.system = LiveOakFactory.create();
        this.client = this.system.client();
        this.system.extensionInstaller().load("config", new InMemoryConfigExtension());

        // LIVEOAK-295 ... make sure system services have all started before performing programmatic application deployment
        this.system.awaitStability();

        this.system.applicationRegistry().createApplication(ZeroExtension.APPLICATION_ID, ZeroExtension.APPLICATION_NAME);
        this.application = this.system.applicationRegistry().createApplication( "testApp", "Test Application" );

        this.system.awaitStability();

        ObjectNode configNode = JsonNodeFactory.instance.objectNode();
        configNode.put(FIRST_KEY, FIRST_VALUE);
        configNode.put(SECOND_KEY, SECOND_VALUE);
        this.application.extend("config", CONFIG_RESOURCE_ID, configNode);
        this.system.awaitStability();
    }

    @After
    public void shutdown() {
        this.system.stop();
    }

    @Test
    public void testReadConfiguration() throws Exception {
        ResourceState configState = this.client.read(new RequestContext.Builder().build(), ADMIN_PATH + CONFIG_RESOURCE_ID);

        assertThat(configState).isNotNull();
        assertThat(configState.id()).isEqualTo(CONFIG_RESOURCE_ID);
        assertThat(configState.uri().toString()).isEqualTo(ADMIN_PATH + CONFIG_RESOURCE_ID);

    }

    @Test
    public void testConfigPropertyString() throws Exception {
        ResourceState configState = this.client.read(new RequestContext.Builder().build(), ADMIN_PATH + CONFIG_RESOURCE_ID);

        assertThat(configState).isNotNull();
        assertThat(configState.id()).isEqualTo(CONFIG_RESOURCE_ID);
        assertThat(configState.uri().toString()).isEqualTo(ADMIN_PATH + CONFIG_RESOURCE_ID);

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
        try {
            ObjectNode configNode = JsonNodeFactory.instance.objectNode();
            configNode.put(SECOND_KEY, SECOND_VALUE);
            this.application.extend("config", "config3", configNode);
            fail("Should throw due to missing config for 'path1'");
        } catch (Exception e) {
            // expected and correct
        }
    }

    @Test
    public void testConfigPropertyConverter() throws Exception {
        this.system.extensionInstaller().load("config-converter", new InMemoryConfigConverterExtension());

        ObjectNode configNode = JsonNodeFactory.instance.objectNode();
        configNode.put("file", this.projectRoot.getAbsolutePath());
        this.application.extend("config-converter", CONFIG_CONVERTER_RESOURCE_ID, configNode);
        this.system.awaitStability();

        ResourceState configState = this.client.read(new RequestContext.Builder().build(), ADMIN_PATH + CONFIG_CONVERTER_RESOURCE_ID);

        assertThat(configState).isNotNull();

        Object value = configState.getProperty("file");
        assertThat(value).isNotNull();
        assertThat(value).isInstanceOf(String.class);
        assertThat(value).toString().equals(this.projectRoot.getAbsolutePath());
    }

    @Test
    public void testConfigMultiPropertyConversion() throws Exception {
        this.system.extensionInstaller().load("config-multi", new InMemoryConfigMultiValueExtension());

        ObjectNode configNode = JsonNodeFactory.instance.objectNode();
        configNode.put("firstValue", "firstValue");
        configNode.put("secondValue", "another");
        this.application.extend("config-multi", CONFIG_MULTI_RESOURCE_ID, configNode);
        this.system.awaitStability();

        ResourceState configState = this.client.read(new RequestContext.Builder().build(), ADMIN_PATH + CONFIG_MULTI_RESOURCE_ID);

        assertThat(configState).isNotNull();

        Object value = configState.getProperty("firstValue");
        assertThat(value).isNotNull();
        assertThat(value).isInstanceOf(String.class);
        assertThat(value).toString().equals("firstValue");

        value = configState.getProperty("secondValue");
        assertThat(value).isNotNull();
        assertThat(value).isInstanceOf(String.class);
        assertThat(value).toString().equals("another");
    }

    @Test
    public void testConfigPropertyTypeConversion() throws Exception {
        this.system.extensionInstaller().load("config-types", new InMemoryConfigTypesExtension());

        ObjectNode configNode = JsonNodeFactory.instance.objectNode();
        configNode.put("file", this.projectRoot.getAbsolutePath());
        configNode.put("flag", true);
        configNode.put("url", "http://liveoak.io");
        configNode.put("uri", "urn:isbn:0451450523");
        configNode.put("dbl", new Double("4.6"));
        configNode.put("integer", new Integer("35"));
        this.application.extend("config-types", CONFIG_TYPE_RESOURCE_ID, configNode);
        this.system.awaitStability();

        ResourceState configState = this.client.read(new RequestContext.Builder().build(), ADMIN_PATH + CONFIG_TYPE_RESOURCE_ID);

        assertThat(configState).isNotNull();

        Object value = configState.getProperty("file");
        assertThat(value).isNotNull();
        assertThat(value).isInstanceOf(String.class);
        assertThat(value).toString().equals(this.projectRoot.getAbsolutePath());

        value = configState.getProperty("flag");
        assertThat(value).isNotNull();
        assertThat(value).isInstanceOf(Boolean.class);
        assertThat(value).isEqualTo(Boolean.TRUE);

        value = configState.getProperty("url");
        assertThat(value).isNotNull();
        assertThat(value).isInstanceOf(String.class);
        assertThat(value).toString().equals("http://liveoak.io");

        value = configState.getProperty("uri");
        assertThat(value).isNotNull();
        assertThat(value).isInstanceOf(String.class);
        assertThat(value).toString().equals("urn:isbn:0451450523");

        value = configState.getProperty("dbl");
        assertThat(value).isNotNull();
        assertThat(value).isInstanceOf(Double.class);
        assertThat(value).toString().equals("4.6");

        value = configState.getProperty("integer");
        assertThat(value).isNotNull();
        assertThat(value).isInstanceOf(Integer.class);
        assertThat(value).toString().equals("35");
    }
}
