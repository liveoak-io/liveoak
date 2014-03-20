package io.liveoak.container;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.liveoak.common.codec.DefaultResourceState;
import io.liveoak.container.extension.MockExtension;
import io.liveoak.container.tenancy.InternalApplication;
import io.liveoak.container.zero.extension.ZeroExtension;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.ResourceException;
import io.liveoak.spi.client.Client;
import io.liveoak.spi.state.ResourceState;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import static org.fest.assertions.Assertions.assertThat;
import static org.junit.Assert.fail;

/**
 * @author Bob McWhirter
 * @author Ken Finnigan
 */
public class ConfigurationPersistingTest {

    private LiveOakSystem system;
    private Client client;
    private InternalApplication application;

    private static final String ADMIN_PATH = "/" + ZeroExtension.APPLICATION_ID + "/applications/testApp/resources/";

    private File projectRoot;
    private ObjectMapper mapper;

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

        this.application = this.system.applicationRegistry().createApplication("testApp", "Test Application");
        this.system.extensionInstaller().load("mock", new MockExtension());
        this.system.awaitStability();

        this.mapper = new ObjectMapper();
        mapper.configure(JsonParser.Feature.ALLOW_COMMENTS, true);
        mapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
        mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
    }

    @After
    public void shutdown() {
        this.system.stop();
    }

    protected ObjectNode readConfig() throws IOException {
        return (ObjectNode) mapper.readTree(this.application.configurationFile());
    }

    @Test
    public void testPersistRuntimeAddedResource() throws Exception {
        ObjectNode tree = readConfig();

        assertThat(tree.get("resources")).isNotNull();
        assertThat(tree.get("resources").get("mock")).isNull();

        ObjectNode initialConfig = JsonNodeFactory.instance.objectNode();
        initialConfig.put("cheese", "cheddar");
        this.application.extend("mock", initialConfig);

        this.system.awaitStability();

        ResourceState configState = this.client.read(new RequestContext.Builder().build(), ADMIN_PATH + "mock");
        assertThat(configState).isNotNull();

        tree = readConfig();
        JsonNode mockTree = tree.get("resources").get("mock");

        assertThat(mockTree).isNotNull();
        assertThat(mockTree.get("type").asText()).isEqualTo("mock");

        JsonNode mockConfig = mockTree.get("config");
        assertThat(mockConfig).isNotNull();
        assertThat(mockConfig.get("cheese").asText()).isEqualTo("cheddar");
    }

    @Test
    public void testRuntimeUpdatedConfiguration() throws Exception {
        ObjectNode initialConfig = JsonNodeFactory.instance.objectNode();
        initialConfig.put("cheese", "cheddar");
        this.application.extend("mock", initialConfig);
        this.system.awaitStability();

        ResourceState configState = this.client.read(new RequestContext.Builder().build(), ADMIN_PATH + "mock");
        assertThat(configState).isNotNull();

        JsonNode tree = readConfig();
        JsonNode mockTree = tree.get("resources").get("mock");

        assertThat(mockTree).isNotNull();
        assertThat(mockTree.get("type").asText()).isEqualTo("mock");

        JsonNode mockConfig = mockTree.get("config");
        assertThat(mockConfig).isNotNull();
        assertThat(mockConfig.get("cheese").asText()).isEqualTo("cheddar");

        configState = new DefaultResourceState();
        configState.putProperty("dog", "moses");

        ResourceState result = this.client.update(new RequestContext.Builder().build(), ADMIN_PATH + "mock", configState);

        assertThat(result).isNotNull();
        assertThat(result.getProperty("cheese")).isNull();
        assertThat(result.getProperty("dog")).isEqualTo("moses");

        tree = readConfig();
        mockTree = tree.get("resources").get("mock");

        assertThat(mockTree).isNotNull();
        assertThat(mockTree.get("type").asText()).isEqualTo("mock");

        mockConfig = mockTree.get("config");
        assertThat(mockConfig).isNotNull();
        assertThat(mockConfig.get("cheese")).isNull();
        assertThat(mockConfig.get("dog").asText()).isEqualTo("moses");
    }

    @Test
    public void testRuntimeRemoval() throws Exception {
        ObjectNode initialConfig = JsonNodeFactory.instance.objectNode();
        initialConfig.put("cheese", "cheddar");
        this.application.extend("mock", initialConfig);
        this.system.awaitStability();

        ResourceState configState = this.client.read(new RequestContext.Builder().build(), ADMIN_PATH + "mock");
        assertThat(configState).isNotNull();

        JsonNode tree = readConfig();
        JsonNode mockTree = tree.get("resources").get("mock");

        assertThat(mockTree).isNotNull();
        assertThat(mockTree.get("type").asText()).isEqualTo("mock");

        this.client.delete(new RequestContext.Builder().build(), ADMIN_PATH + "mock");

        try {
            this.client.read(new RequestContext.Builder().build(), ADMIN_PATH + "mock");
            fail("should be gone");
        } catch (ResourceException e) {
            // expected
        }

        tree = readConfig();
        mockTree = tree.get("resources").get("mock");

        assertThat( mockTree ).isNull();
    }
}
