package io.liveoak.application.clients;

import java.io.File;
import java.io.IOException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.liveoak.application.clients.extension.ApplicationClientsExtension;
import io.liveoak.container.tenancy.ApplicationConfigurationManager;
import io.liveoak.spi.Services;
import io.liveoak.spi.util.ObjectMapperFactory;
import io.liveoak.testtools.AbstractHTTPResourceTestCaseWithTestApp;
import org.jboss.msc.service.ValueService;
import org.jboss.msc.value.ImmediateValue;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;

import static io.liveoak.testtools.assertions.Assertions.assertThat;

/**
 * @author Ken Finnigan
 */
public class ApplicationClientsTest extends AbstractHTTPResourceTestCaseWithTestApp {

    private File appConfigFile;

    @BeforeClass
    public static void setUp() throws Exception {
        system.serviceTarget().addService(Services.SECURITY_CLIENT, new ValueService<>(new ImmediateValue<>(new MockSecurityClient()))).install();
        system.serviceTarget().addService(Services.SECURITY_DIRECT_ACCESS_CLIENT, new ValueService<>(new ImmediateValue<>(new MockDirectAccessClient()))).install();
        loadExtension("application-clients", new ApplicationClientsExtension());
    }

    @After
    public void removeResource() throws Exception {
        assertThat(execDelete("/admin/applications/testApp/resources/application-clients")).hasStatus(200);
        awaitStability();
        if (appConfigFile != null) {
            assertThat(appConfigFile.exists()).isFalse();
        }
    }

    protected JsonNode installResource() throws Exception {
        assertThat(execPost("/admin/applications/testApp/resources", "{ \"id\" : \"application-clients\", \"type\" : \"application-clients\" }")).hasStatus(201);
        JsonNode result = toJSON(httpResponse.getEntity());
        assertThat(result.get("id").asText()).isEqualTo("application-clients");
        assertThat(result.get("type").asText()).isEqualTo("application-clients");
        awaitStability();

        appConfigFile = new File(testApplication.directory(), "config/application-clients.json");

        return result;
    }

    protected JsonNode read() throws IOException {
        ObjectMapper mapper = ObjectMapperFactory.create();
        return mapper.readTree(appConfigFile);
    }

    @Test
    public void install() throws Exception {
        // Verify resource is not installed
        assertThat(getJSON("/admin/applications/testApp/resources").get("application-clients")).isNull();

        // Install application-clients resource
        installResource();

        JsonNode result = getJSON("/admin/applications/testApp/resources/application-clients");

        assertThat(result.get("id").asText()).isEqualTo("application-clients");
        assertThat(result.get("type").asText()).isEqualTo("application-clients");

        assertThat(appConfigFile.exists()).isFalse();

        // Verify application.json updated
        File appJson = new File(testApplication.directory(), "application.json");
        ApplicationConfigurationManager mgr = new ApplicationConfigurationManager(appJson);
        ObjectNode node = mgr.readResource("application-clients");
        assertThat(node).isNotNull();
    }

    @Test
    public void addApplicationClient() throws Exception {
        // Install application-clients resource
        installResource();

        // Add application client
        assertThat(execPost("/admin/applications/testApp/resources/application-clients",
                "{ " +
                        "\"id\" : \"html-app-client\", " +
                        "\"type\" : \"html5\", " +
                        "\"redirect-uris\" : [" +
                            "\"/html-client/*\"," +
                            "\"/html-client\"" +
                        "]," +
                        "\"web-origins\" : [" +
                            "\"http://localhost:8080\" " +
                        "]," +
                        "\"app-roles\" : [" +
                            "\"user\"" +
                        "]" +
                "}"))
                .hasStatus(201);
        JsonNode result = toJSON(httpResponse.getEntity());

        Verify.appClient(result, "html-app-client", "html5", new String[]{"/html-client/*", "/html-client"}, new String[]{"http://localhost:8080"}, new String[]{"user"});

        // Verify JSON file
        assertThat(appConfigFile.exists()).isTrue();
        JsonNode node = read();
        assertThat(node.isArray()).isTrue();
        ArrayNode array = (ArrayNode) node;
        assertThat(array.size()).isEqualTo(1);
        ObjectNode configNode = (ObjectNode) array.get(0);
        Verify.appClient(configNode, "html-app-client", "html5", new String[]{"/html-client/*", "/html-client"}, new String[]{"http://localhost:8080"}, new String[]{"user"});
    }

    @Test
    public void addApplicationClientFailsWithNonUnique() throws Exception {
        // Install application-clients resource
        installResource();

        // Add application client
        assertThat(execPost("/admin/applications/testApp/resources/application-clients",
                "{ " +
                        "\"id\" : \"html-app-client\", " +
                        "\"type\" : \"html5\", " +
                        "\"redirect-uris\" : [" +
                            "\"/html-client/*\"," +
                            "\"/html-client\"" +
                        "]," +
                        "\"web-origins\" : [" +
                            "\"http://localhost:8080\" " +
                        "]," +
                        "\"app-roles\" : [" +
                            "\"user\"" +
                        "]" +
                        "}"))
                .hasStatus(201);
        JsonNode result = toJSON(httpResponse.getEntity());

        Verify.appClient(result, "html-app-client", "html5", new String[]{"/html-client/*", "/html-client"}, new String[]{"http://localhost:8080"}, new String[]{"user"});

        // Verify JSON file
        assertThat(appConfigFile.exists()).isTrue();
        JsonNode node = read();
        assertThat(node.isArray()).isTrue();
        ArrayNode array = (ArrayNode) node;
        assertThat(array.size()).isEqualTo(1);
        ObjectNode configNode = (ObjectNode) array.get(0);
        Verify.appClient(configNode, "html-app-client", "html5", new String[]{"/html-client/*", "/html-client"}, new String[]{"http://localhost:8080"}, new String[]{"user"});

        // Add application client that should fail
        assertThat(execPost("/admin/applications/testApp/resources/application-clients",
                "{ " +
                        "\"id\" : \"html-app-client\", " +
                        "\"type\" : \"html5\", " +
                        "\"redirect-uris\" : [" +
                            "\"/html-client/*\"," +
                            "\"/html-client\"" +
                        "]," +
                        "\"web-origins\" : [" +
                            "\"http://localhost:8080\" " +
                        "]," +
                        "\"app-roles\" : [" +
                            "\"user\"" +
                        "]" +
                        "}"))
                .hasStatus(406);
    }

    @Test
    public void updateApplicationClient() throws Exception {
        // Install application-clients resource
        installResource();

        // Add application client
        assertThat(execPost("/admin/applications/testApp/resources/application-clients",
                "{ " +
                        "\"id\" : \"html-app-client\", " +
                        "\"type\" : \"html5\", " +
                        "\"redirect-uris\" : [" +
                            "\"/html-client/*\"," +
                            "\"/html-client\"" +
                        "]," +
                        "\"web-origins\" : [" +
                            "\"http://localhost:8080\" " +
                        "]," +
                        "\"app-roles\" : [" +
                            "\"user\"" +
                        "]" +
                        "}"))
                .hasStatus(201);
        JsonNode result = toJSON(httpResponse.getEntity());

        Verify.appClient(result, "html-app-client", "html5", new String[]{"/html-client/*", "/html-client"}, new String[]{"http://localhost:8080"}, new String[]{"user"});

        // Verify JSON file
        assertThat(appConfigFile.exists()).isTrue();
        JsonNode node = read();
        assertThat(node.isArray()).isTrue();
        ArrayNode array = (ArrayNode) node;
        assertThat(array.size()).isEqualTo(1);
        ObjectNode configNode = (ObjectNode) array.get(0);
        Verify.appClient(configNode, "html-app-client", "html5", new String[]{"/html-client/*", "/html-client"}, new String[]{"http://localhost:8080"}, new String[]{"user"});

        // Update application client
        assertThat(execPut("/admin/applications/testApp/resources/application-clients/html-app-client",
                "{ " +
                        "\"type\" : \"ios\", " +
                        "\"redirect-uris\" : [" +
                            "\"/html-ios/*\"," +
                            "\"/html-client\"" +
                        "]," +
                        "\"web-origins\" : [" +
                            "\"http://localhost:8080\" " +
                        "]," +
                        "\"app-roles\" : [" +
                            "\"user\"" +
                        "]" +
                        "}"))
                .hasStatus(200);
        result = toJSON(httpResponse.getEntity());
        Verify.appClient(result, "html-app-client", "ios", new String[]{"/html-ios/*", "/html-client"}, new String[]{"http://localhost:8080"}, new String[]{"user"});

        // Verify JSON file
        assertThat(appConfigFile.exists()).isTrue();
        node = read();
        assertThat(node.isArray()).isTrue();
        array = (ArrayNode) node;
        assertThat(array.size()).isEqualTo(1);
        configNode = (ObjectNode) array.get(0);
        Verify.appClient(configNode, "html-app-client", "ios", new String[]{"/html-ios/*", "/html-client"}, new String[]{"http://localhost:8080"}, new String[]{"user"});
    }

    @Test
    public void deleteApplicationClientFailsAsNotFound() throws Exception {
        // Install application-clients resource
        installResource();

        assertThat(execDelete("/admin/applications/testApp/resources/application-clients/my-html-client")).hasStatus(404);

        // Add application client
        assertThat(execPost("/admin/applications/testApp/resources/application-clients",
                "{ " +
                        "\"id\" : \"html-app-client\", " +
                        "\"type\" : \"html5\", " +
                        "\"redirect-uris\" : [" +
                            "\"/html-client/*\"," +
                            "\"/html-client\"" +
                        "]," +
                        "\"web-origins\" : [" +
                            "\"http://localhost:8080\" " +
                        "]," +
                        "\"app-roles\" : [" +
                            "\"user\"" +
                        "]" +
                        "}"))
                .hasStatus(201);
        JsonNode result = toJSON(httpResponse.getEntity());
        Verify.appClient(result, "html-app-client", "html5", new String[]{"/html-client/*", "/html-client"}, new String[]{"http://localhost:8080"}, new String[]{"user"});

        assertThat(execDelete("/admin/applications/testApp/resources/application-clients/my-html-client")).hasStatus(404);
    }

    @Test
    public void deleteResourceTest() throws Exception {
        // Install application-clients resource
        installResource();

        // Add application client
        assertThat(execPost("/admin/applications/testApp/resources/application-clients",
                "{ " +
                        "\"id\" : \"html-app-client\", " +
                        "\"type\" : \"html5\", " +
                        "\"redirect-uris\" : [" +
                            "\"/html-client/*\"," +
                            "\"/html-client\"" +
                        "]," +
                        "\"web-origins\" : [" +
                            "\"http://localhost:8080\" " +
                        "]," +
                        "\"app-roles\" : [" +
                            "\"user\"" +
                        "]" +
                        "}"))
                .hasStatus(201);
        JsonNode result = toJSON(httpResponse.getEntity());
        Verify.appClient(result, "html-app-client", "html5", new String[]{"/html-client/*", "/html-client"}, new String[]{"http://localhost:8080"}, new String[]{"user"});

        // Verify JSON file
        assertThat(appConfigFile.exists()).isTrue();
        JsonNode node = read();
        assertThat(node.isArray()).isTrue();
        ArrayNode array = (ArrayNode) node;
        assertThat(array.size()).isEqualTo(1);
        ObjectNode configNode = (ObjectNode) array.get(0);
        Verify.appClient(configNode, "html-app-client", "html5", new String[]{"/html-client/*", "/html-client"}, new String[]{"http://localhost:8080"}, new String[]{"user"});

        removeResource();

        assertThat(execGet("/admin/applications/testApp/resources/application-clients")).hasStatus(404);
        assertThat(appConfigFile.exists()).isFalse();

        // Hack to make @After work
        installResource();
    }
}

