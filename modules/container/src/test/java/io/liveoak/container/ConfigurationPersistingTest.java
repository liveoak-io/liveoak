package io.liveoak.container;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.liveoak.common.codec.DefaultResourceState;
import io.liveoak.container.extension.FilterExtension;
import io.liveoak.container.extension.MockExtension;
import io.liveoak.container.tenancy.InternalApplication;
import io.liveoak.container.zero.extension.ZeroExtension;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.ResourceException;
import io.liveoak.spi.client.Client;
import io.liveoak.spi.state.ResourceState;
import org.fest.assertions.Condition;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

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

    private ObjectMapper mapper;

    @Before
    public void setUp() throws Exception {
        this.system = LiveOakFactory.create();
        this.client = this.system.client();

        this.system.extensionInstaller().load("mock", new MockExtension());
        this.system.extensionInstaller().load("filter", new FilterExtension());

        // LIVEOAK-295 ... make sure system services have all started before performing programmatic application deployment
        this.system.awaitStability();

        this.system.applicationRegistry().createApplication(ZeroExtension.APPLICATION_ID, ZeroExtension.APPLICATION_NAME);
        this.application = this.system.applicationRegistry().createApplication("testApp", "Test Application");

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
        System.err.println( "read-from-disk: " + this.application.configurationFile() + " // " + this.application.configurationFile().exists() );
        if ( this.application.configurationFile().exists() ) {
            System.err.println( "file size: " + this.application.configurationFile().length() );
        }
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

    @Test
    public void testReadNonRuntimeValues() throws Exception {
        String appDir = "${application.dir}/app/";
        String randomDir = "/my/path/${application.name}/random";
        ObjectNode tree = readConfig();

        assertThat(tree.get("resources")).isNotNull();
        assertThat(tree.get("resources").get("filter")).isNull();

        ObjectNode initialConfig = JsonNodeFactory.instance.objectNode();
        initialConfig.put("appDir", appDir);
        initialConfig.put("randomDir", randomDir);
        this.application.extend("filter", initialConfig);

        this.system.awaitStability();

        // Check config values to make sure we're receiving the unparsed versions
        ResourceState configState = this.client.read(new RequestContext.Builder().build(), ADMIN_PATH + "filter");
        assertThat(configState).isNotNull();
        assertThat(configState.getProperty("appDir")).isEqualTo(appDir);
        assertThat(configState.getProperty("randomDir")).isEqualTo(randomDir);

        // Check that the Resource itself is dealing with parsed values
        configState = this.client.read(new RequestContext.Builder().build(), "/testApp/filter");
        assertThat(configState).isNotNull();
        assertThat(configState.getProperty("appDir")).isNotEqualTo(appDir);
        assertThat(configState.getProperty("randomDir")).isNotEqualTo(randomDir);
        assertThat(configState.getProperty("appDir")).satisfies(new Condition<Object>() {
            @Override
            public boolean matches(Object value) {
                if (!(value instanceof String)) {
                    return false;
                }
                return ((String) value).endsWith("/app/");
            }
        });

        // Check contents of application.json
        tree = readConfig();
        JsonNode filterTree = tree.get("resources").get("filter");

        assertThat(filterTree).isNotNull();
        assertThat(filterTree.get("type").asText()).isEqualTo("filter");
        JsonNode configTree = filterTree.get("config");
        assertThat(configTree.get("appDir").asText()).isEqualTo(appDir);
        assertThat(configTree.get("randomDir").asText()).isEqualTo(randomDir);

        // Check config values to make sure we can request the parsed versions
        configState = this.client.read(new RequestContext.Builder().build(), ADMIN_PATH + "filter?runtime");
        assertThat(configState).isNotNull();
        assertThat(configState.getProperty("appDir")).isNotEqualTo(appDir);
        assertThat(configState.getProperty("randomDir")).isNotEqualTo(randomDir);
        assertThat(configState.getProperty("appDir")).satisfies(new Condition<Object>() {
            @Override
            public boolean matches(Object value) {
                if (!(value instanceof String)) {
                    return false;
                }
                return ((String) value).endsWith("/app/");
            }
        });
        assertThat(configState.getProperty("randomDir")).satisfies(new Condition<Object>() {
            @Override
            public boolean matches(Object value) {
                if (!(value instanceof String)) {
                    return false;
                }
                return ((String) value).endsWith("/random");
            }
        });

        // Update one of the configuration values
        String modifiedDir = randomDir + "/more/";
        configState = new DefaultResourceState();
        configState.putProperty("appDir", appDir);
        configState.putProperty("randomDir", modifiedDir);

        this.client.update(new RequestContext.Builder().build(), ADMIN_PATH + "filter", configState);

        // Check config values to make sure we're receiving the unparsed versions
        configState = this.client.read(new RequestContext.Builder().build(), ADMIN_PATH + "filter");
        assertThat(configState).isNotNull();
        assertThat(configState.getProperty("appDir")).isEqualTo(appDir);
        assertThat(configState.getProperty("randomDir")).isEqualTo(modifiedDir);

        // Check that the Resource itself is dealing with parsed values
        configState = this.client.read(new RequestContext.Builder().build(), "/testApp/filter");
        assertThat(configState).isNotNull();
        assertThat(configState.getProperty("appDir")).isNotEqualTo(appDir);
        assertThat(configState.getProperty("randomDir")).isNotEqualTo(randomDir);
        assertThat(configState.getProperty("appDir")).satisfies(new Condition<Object>() {
            @Override
            public boolean matches(Object value) {
                if (!(value instanceof String)) {
                    return false;
                }
                return ((String) value).endsWith("/app/");
            }
        });
        assertThat(configState.getProperty("randomDir")).satisfies(new Condition<Object>() {
            @Override
            public boolean matches(Object value) {
                if (!(value instanceof String)) {
                    return false;
                }
                return ((String) value).endsWith("/random/more/");
            }
        });

        // Check contents of application.json
        tree = readConfig();
        filterTree = tree.get("resources").get("filter");

        assertThat(filterTree).isNotNull();
        assertThat(filterTree.get("type").asText()).isEqualTo("filter");
        configTree = filterTree.get("config");
        assertThat(configTree.get("appDir").asText()).isEqualTo(appDir);
        assertThat(configTree.get("randomDir").asText()).isEqualTo(modifiedDir);
    }
}
