package io.liveoak.container.zero;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.liveoak.applications.templates.TemplateExtension;
import io.liveoak.container.AbstractContainerTest;
import io.liveoak.container.LiveOakFactory;
import io.liveoak.container.extension.MockExtension;
import io.liveoak.spi.MediaType;
import io.liveoak.spi.util.ObjectMapperFactory;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.*;

import java.io.File;
import java.io.InputStream;

import static org.fest.assertions.Assertions.assertThat;

/**
 * Created by mwringe on 12/02/15.
 */
public class TemplateApplicationResourceTest extends AbstractContainerTest {

    private CloseableHttpClient httpClient;
    private static File appDir;

    @BeforeClass
    public static void setUpServer() throws Exception {
        appDir = new File(LocalApplicationsResourceTest.class.getClassLoader().getResource("apps").getFile());
        system = LiveOakFactory.create(null, appDir, null);

        system.extensionInstaller().load("aggregating-filesystem", new MockExtension("aggr-filesystem"));
        system.extensionInstaller().load("filesystem", new MockExtension("filesystem"));
        system.extensionInstaller().load("mongo", new MockExtension("mongo"));

        File templateConfig = new File(LocalApplicationsResourceTest.class.getClassLoader().getResource("templateApps/application-templates.json").getFile());
        system.extensionInstaller().loadFile(templateConfig, new TemplateExtension());

        system.awaitStability();
        awaitStability();
    }

    @AfterClass
    public static void tearDownServer() throws Exception {
        system.stop();
        System.err.flush();
    }

    @Before
    public void setUpClient() throws Exception {
        this.httpClient = HttpClientBuilder.create().build();
    }

    @After
    public void tearDownClient() throws Exception {
        this.httpClient.close();
    }

    @AfterClass
    public static void cleanUpInstalledApps() throws Exception {
        new AppCleanup().accept("apps/mytemplateappA");
        new AppCleanup().accept("apps/mytemplateappB");
        new AppCleanup().accept("apps/mytemplateappC");
        new AppCleanup().accept("apps/mytemplateappD");
        new AppCleanup().accept("apps/mytemplateappfoo");
    }

    @Test
    public void templateCreateNoTemplate() throws Exception {
        defaultTemplateTest("mytemplateappA", "My Template Application A", "default",
                "{ \"id\": \"mytemplateappA\", \"name\": \"My Template Application A\"}");
    }

    @Test
    public void templateCreateModule() throws Exception {
        defaultTemplateTest("mytemplateappB", "My Template Application B", "default",
                "{ \"id\": \"mytemplateappB\", \"name\": \"My Template Application B\", \"template\":\"module\"}");
    }

    @Test
    public void templateCreateWithToken() throws Exception {
        defaultTemplateTest("mytemplateappC", "My Template Application C", "stuff",
                "{ \"id\": \"mytemplateappC\", \"name\": \"My Template Application C\", \"template\":\"module\"," +
                        "\"tokens\": {\"database-name\":\"stuff\"}}");
    }

    @Test
    public void templateCreateNoName() throws Exception {
        defaultTemplateTest("mytemplateappD", "mytemplateappD", "default",
                "{ \"id\": \"mytemplateappD\"}");
    }

    protected void defaultTemplateTest(String appId, String appName, String dbName, String jsonConfig) throws Exception {
        CloseableHttpResponse response;

        response = post("http://localhost:8080/admin/applications", jsonConfig);

        assertThat(response).isNotNull();
        assertThat(response.getStatusLine().getStatusCode()).isEqualTo(201);

        ObjectNode objectNode = (ObjectNode) toJsonNode(response);
        assertThat(objectNode.get("id").asText()).isEqualTo(appId);
        assertThat(objectNode.get("name").asText()).isEqualTo(appName);

        ObjectNode resources = (ObjectNode) get("http://localhost:8080/admin/applications/" + appId + "/resources?fields=*(*)");

        ArrayNode members = (ArrayNode) resources.get("members");
        assertThat(members).hasSize(1);

        ObjectNode storage = (ObjectNode) members.get(0);
        assertThat(storage.get("id").asText()).isEqualTo("storage");
        assertThat(storage.get("db").asText()).isEqualTo(dbName);
        response.close();
    }

    @Test
    public void testCreateFoo() throws Exception {
        CloseableHttpResponse response;

        response = post("http://localhost:8080/admin/applications", "{ \"id\": \"mytemplateappfoo\", \"name\": \"My Foo Template Application\", \"template\":\"foo\"}");

        assertThat(response).isNotNull();
        assertThat(response.getStatusLine().getStatusCode()).isEqualTo(201);

        ObjectNode objectNode = (ObjectNode) toJsonNode(response);
        assertThat(objectNode.get("id").asText()).isEqualTo("mytemplateappfoo");
        assertThat(objectNode.get("html-app").asText()).isEqualTo("/mytemplateappfoo/foo");

        response.close();
    }


    protected JsonNode toJsonNode(HttpResponse response) throws Exception {
        InputStream inputStream = response.getEntity().getContent();
        return ObjectMapperFactory.create().readTree(inputStream);
    }

    protected JsonNode get(String url) throws Exception {
        HttpGet getRequest = new HttpGet(url);
        return toJsonNode(httpClient.execute(getRequest));
    }

    protected CloseableHttpResponse post(String url, String json) throws Exception {
        HttpPost postRequest = new HttpPost(url);
        postRequest.setEntity(new StringEntity(json));
        postRequest.setHeader("Content-Type", MediaType.TEMPLATE_APP_JSON.toString());

        return httpClient.execute(postRequest);
    }

}
