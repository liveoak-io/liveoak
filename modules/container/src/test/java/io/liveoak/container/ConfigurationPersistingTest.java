package io.liveoak.container;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.liveoak.common.DefaultResourceParams;
import io.liveoak.common.codec.DefaultResourceState;
import io.liveoak.common.util.ConversionUtils;
import io.liveoak.container.extension.FilterExtension;
import io.liveoak.container.extension.MockExtension;
import io.liveoak.container.tenancy.InternalApplication;
import io.liveoak.container.zero.extension.ZeroExtension;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.exceptions.ResourceException;
import io.liveoak.spi.ResourceParams;
import io.liveoak.spi.client.Client;
import io.liveoak.spi.resource.async.Resource;
import io.liveoak.spi.state.ResourceState;
import org.fest.assertions.Condition;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

        this.application.extend("filter", ConversionUtils.convert(buildConfig(appDir, randomDir)));

        this.system.awaitStability();

        // Check config values to make sure we're receiving the unparsed versions
        ResourceState configState = this.client.read(new RequestContext.Builder().build(), ADMIN_PATH + "filter");
        validateEquals(configState, appDir, randomDir);

        // Check that the Resource itself is dealing with parsed values
        configState = this.client.read(new RequestContext.Builder().build(), "/testApp/filter");
        validateNotEquals(configState, appDir, randomDir, "");

        // Check contents of application.json
        validateFile(appDir, randomDir);

        // Check config values to make sure we can request the parsed versions, when runtime present but not value
        Map<String, List<String>> params = new HashMap<>();
        params.put("runtime", new ArrayList<>());
        ResourceParams resourceParams = DefaultResourceParams.instance(params);
        RequestContext requestContext = new RequestContext.Builder().resourceParams(resourceParams).build();
        configState = this.client.read(requestContext, ADMIN_PATH + "filter");
        validateNotEquals(configState, appDir, randomDir, "");

        // Check config values to make sure we can request the parsed versions, when runtime present and value is true
        params.get("runtime").add("true");
        resourceParams = DefaultResourceParams.instance(params);
        requestContext = new RequestContext.Builder().resourceParams(resourceParams).build();
        configState = this.client.read(requestContext, ADMIN_PATH + "filter");
        validateNotEquals(configState, appDir, randomDir, "");

        // Check config values to make sure we can request the non parsed versions, when runtime present and value is false
        params.put("runtime", new ArrayList<>());
        params.get("runtime").add("false");
        resourceParams = DefaultResourceParams.instance(params);
        requestContext = new RequestContext.Builder().resourceParams(resourceParams).build();
        configState = this.client.read(requestContext, ADMIN_PATH + "filter");
        validateEquals(configState, appDir, randomDir);

        // Update one of the configuration values
        String modifiedDir = randomDir + "/more/";

        this.client.update(new RequestContext.Builder().build(), ADMIN_PATH + "filter", buildConfig(appDir, modifiedDir));

        // Check config values to make sure we're receiving the unparsed versions
        configState = this.client.read(new RequestContext.Builder().build(), ADMIN_PATH + "filter");
        validateEquals(configState, appDir, modifiedDir);

        // Check that the Resource itself is dealing with parsed values
        configState = this.client.read(new RequestContext.Builder().build(), "/testApp/filter");
        validateNotEquals(configState, appDir, modifiedDir, "/more/");

        // Check contents of application.json
        validateFile(appDir, modifiedDir);
    }

    private ResourceState buildConfig(String appDir, String randomDir) {
        ResourceState subState = new DefaultResourceState();
        subState.putProperty("appDir", "/my/app/dir");
        subState.putProperty("subAppDir", appDir);
        List<ResourceState> subList = new ArrayList<>();
        subList.add(subState);

        ResourceState configState = new DefaultResourceState();
        configState.putProperty("appDir", appDir);
        configState.putProperty("randomDir", randomDir);
        configState.putProperty("notVar", "testApp");
        configState.putProperty("sub", subList);

        return configState;
    }

    private void validateFile(String expectedAppDir, String expectedRandomDir) throws IOException {
        JsonNode filterTree = readConfig().get("resources").get("filter");

        assertThat(filterTree).isNotNull();
        assertThat(filterTree.get("type").asText()).isEqualTo("filter");
        JsonNode configTree = filterTree.get("config");
        assertThat(configTree.get("appDir").asText()).isEqualTo(expectedAppDir);
        assertThat(configTree.get("randomDir").asText()).isEqualTo(expectedRandomDir);
        assertThat(configTree.get("notVar").asText()).isEqualTo("testApp");
        assertThat(configTree.get("sub").get(0).get("appDir").asText()).isEqualTo("/my/app/dir");
        assertThat(configTree.get("sub").get(0).get("subAppDir").asText()).isEqualTo(expectedAppDir);
    }

    private void validateEquals(ResourceState configState, String expectedAppDir, String expectedRandomDir) {
        assertThat(configState).isNotNull();
        assertThat(configState.getProperty("appDir")).isEqualTo(expectedAppDir);
        assertThat(configState.getProperty("randomDir")).isEqualTo(expectedRandomDir);
        assertThat(configState.getProperty("unknownDir")).isEqualTo("/my/unknown/path");
        assertThat(configState.getProperty("notVar")).isEqualTo("testApp");
        assertThat(((ResourceState)((List<Resource>)configState.getProperty("sub")).get(0)).getProperty("appDir")).isEqualTo("/my/app/dir");
        assertThat(((ResourceState)((List<Resource>)configState.getProperty("sub")).get(0)).getProperty("subAppDir")).isEqualTo(expectedAppDir);
    }

    private void validateNotEquals(ResourceState configState, String expectedAppDir, String expectedRandomDir, String extraRandomEnding) {
        assertThat(configState).isNotNull();
        assertThat(configState.getProperty("appDir")).isNotEqualTo(expectedAppDir);
        assertThat(configState.getProperty("randomDir")).isNotEqualTo(expectedRandomDir);
        assertThat(configState.getProperty("unknownDir")).isEqualTo("/my/unknown/path");
        assertThat(configState.getProperty("notVar")).isEqualTo("testApp");
        assertThat(((ResourceState)((List<Resource>)configState.getProperty("sub")).get(0)).getProperty("appDir")).isEqualTo("/my/app/dir");
        assertThat(((ResourceState)((List<Resource>)configState.getProperty("sub")).get(0)).getProperty("subAppDir")).isNotEqualTo(expectedAppDir);
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
                return ((String) value).endsWith("/random" + extraRandomEnding);
            }
        });
        assertThat(((ResourceState)((List<Resource>)configState.getProperty("sub")).get(0)).getProperty("subAppDir")).satisfies(new Condition<Object>() {
            @Override
            public boolean matches(Object value) {
                if (!(value instanceof String)) {
                    return false;
                }
                return ((String) value).endsWith("/app/");
            }
        });
    }
}
