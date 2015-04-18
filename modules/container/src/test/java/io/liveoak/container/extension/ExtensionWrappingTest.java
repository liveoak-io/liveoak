package io.liveoak.container.extension;

import java.io.File;

import io.liveoak.container.AbstractContainerTest;
import io.liveoak.container.LiveOakFactory;
import io.liveoak.container.zero.AppCleanup;
import io.liveoak.spi.MediaType;
import io.liveoak.spi.state.ResourceState;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
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
public class ExtensionWrappingTest extends AbstractContainerTest {

    private CloseableHttpClient httpClient;
    private static File appDir;

    @BeforeClass
    public static void setup() throws Exception {
        appDir = new File(ExtensionWrappingTest.class.getClassLoader().getResource("apps").getFile());
        system = LiveOakFactory.create(null, appDir, preWaitSetupConsumer());
        system.extensionInstaller().load("mock", new MockExtension());

        awaitStability();

        system.applicationRegistry().createApplication("testApp", "Test Application 1");
        awaitStability();
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

    @AfterClass
    public static void cleanUpInstalledApps() throws Exception {
        new AppCleanup().accept("apps/testApp");
    }

    @Test
    public void appExtensionIdReturned() throws Exception {
        HttpPost post = new HttpPost("http://localhost:8080/admin/applications/testApp/resources");
        post.setEntity(new StringEntity("{ \"id\": \"mymock\", \"type\": \"mock\" }"));
        post.setHeader("Content-Type", MediaType.JSON.toString());

        CloseableHttpResponse response = httpClient.execute(post);

        assertThat(response).isNotNull();
        assertThat(response.getStatusLine().getStatusCode()).isEqualTo(201);
        response.close();
        awaitStability();

        ResourceState state = decode(response);

        assertThat(state.id()).isEqualTo("mymock");
        assertThat(state.getPropertyAsString("type")).isEqualTo("mock");

        HttpGet get = new HttpGet("http://localhost:8080/admin/applications/testApp/resources/mymock");

        response = httpClient.execute(get);

        assertThat(response).isNotNull();
        assertThat(response.getStatusLine().getStatusCode()).isEqualTo(200);
        response.close();
        awaitStability();

        state = decode(response);

        assertThat(state.id()).isEqualTo("mymock");
        assertThat(state.getPropertyAsString("type")).isEqualTo("mock");
    }

    @Test
    public void systemExtensionIdReturned() throws Exception {
        HttpGet get = new HttpGet("http://localhost:8080/admin/system/mock?fields=*(*)");

        CloseableHttpResponse response = httpClient.execute(get);

        assertThat(response).isNotNull();
        assertThat(response.getStatusLine().getStatusCode()).isEqualTo(200);
        response.close();
        awaitStability();

        ResourceState state = decode(response);

        assertThat(state.id()).isEqualTo("mock");
        assertThat(state.member("module").getPropertyAsString("type")).isEqualTo("mock");
    }
}
