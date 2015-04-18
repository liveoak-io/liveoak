package io.liveoak.container.extension;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.liveoak.container.AbstractContainerTest;
import io.liveoak.container.LiveOakFactory;
import io.liveoak.spi.MediaType;
import io.liveoak.spi.state.ResourceState;
import io.liveoak.spi.util.ObjectMapperFactory;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;

/**
 * @author Ken Finnigan
 */
public class ExtensionConfigPersistingTest extends AbstractContainerTest {

    private CloseableHttpClient httpClient;
    private static File appDir;
    private static File mockExtensionFile;
    private static File dummyExtensionFile;

    @BeforeClass
    public static void setup() throws Exception {
        appDir = new File(ExtensionWrappingTest.class.getClassLoader().getResource("apps").getFile());
        File tempDir = Files.createTempDirectory("liveoak").toFile();
        system = LiveOakFactory.create(null, appDir, preWaitSetupConsumer());
        mockExtensionFile = new File(tempDir, "mock.json");
        write(mockExtensionFile, getMockConfig());
        dummyExtensionFile = new File(tempDir, "dummy.json");
        write(dummyExtensionFile, getDummyConfig());
        system.extensionInstaller().load(new MockExtension("first"), mockExtensionFile);
        system.extensionInstaller().load(new MockExtension("second"), dummyExtensionFile);

        awaitStability();
    }

    private static ObjectNode getMockConfig() {
        ObjectNode node = ObjectMapperFactory.create().createObjectNode();
        node.put("module-id", "io.liveoak.mock");
        return node;
    }

    private static ObjectNode getDummyConfig() {
        ObjectNode node = ObjectMapperFactory.create().createObjectNode();
        node.put("module-id", "io.liveoak.dummy");
        return node;
    }

    private static void write(File file, ObjectNode tree) throws IOException {
        ObjectMapper mapper = ObjectMapperFactory.create();
        ObjectWriter writer = mapper.writer();
        writer = writer.with(new DefaultPrettyPrinter("\\n"));
        writer.writeValue(file, tree);
    }

    private ObjectNode read(File file) throws IOException {
        ObjectMapper mapper = ObjectMapperFactory.create();
        return (ObjectNode) mapper.readTree(file);
    }

    @AfterClass
    public static void tearDown() throws Exception {
        system.stop();
    }

    @Before
    public void setUpClient() throws Exception {
        this.httpClient = HttpClientBuilder.create().build();
    }

    @After
    public void tearDownClient() throws Exception {
        this.httpClient.close();
    }

    @Test
    public void updateModuleConfig() throws Exception {
        HttpPut put = new HttpPut("http://localhost:8080/admin/system/mock/module");
        put.setEntity(new StringEntity("{ \"key\": \"value\" }"));
        put.setHeader("Content-Type", MediaType.JSON.toString());

        CloseableHttpResponse response = httpClient.execute(put);

        assertThat(response).isNotNull();
        assertThat(response.getStatusLine().getStatusCode()).isEqualTo(200);
        response.close();
        awaitStability();

        ResourceState state = decode(response);
        assertThat(state.getPropertyAsString("key")).isEqualTo("value");

        // Check file
        ObjectNode node = read(mockExtensionFile);
        assertThat(node.get("module-id").asText()).isEqualTo("io.liveoak.mock");
        assertThat(node.get("config").get("key").asText()).isEqualTo("value");

        // Check dummy extension
        HttpGet get = new HttpGet("http://localhost:8080/admin/system/dummy/module");

        response = httpClient.execute(get);

        assertThat(response).isNotNull();
        assertThat(response.getStatusLine().getStatusCode()).isEqualTo(200);
        response.close();
        awaitStability();

        state = decode(response);
        assertThat(state.getPropertyAsString("key")).isNull();

        // Check file
        node = read(dummyExtensionFile);
        assertThat(node.get("module-id").asText()).isEqualTo("io.liveoak.dummy");
        assertThat(node.get("config")).isNull();

        // Update other extension
        put = new HttpPut("http://localhost:8080/admin/system/dummy/module");
        put.setEntity(new StringEntity("{ \"name\": \"john\" }"));
        put.setHeader("Content-Type", MediaType.JSON.toString());

        response = httpClient.execute(put);

        assertThat(response).isNotNull();
        assertThat(response.getStatusLine().getStatusCode()).isEqualTo(200);
        response.close();
        awaitStability();

        state = decode(response);
        assertThat(state.getPropertyAsString("name")).isEqualTo("john");

        // Check file
        node = read(dummyExtensionFile);
        assertThat(node.get("module-id").asText()).isEqualTo("io.liveoak.dummy");
        assertThat(node.get("config").get("name").asText()).isEqualTo("john");

        // Check non updated file
        node = read(mockExtensionFile);
        assertThat(node.get("module-id").asText()).isEqualTo("io.liveoak.mock");
        assertThat(node.get("config").get("key").asText()).isEqualTo("value");
        assertThat(node.get("config").get("name")).isNull();
    }
}
