package io.liveoak.container.tenancy;

import java.io.File;

import io.liveoak.container.InMemoryDBExtension;
import io.liveoak.container.LiveOakFactory;
import io.liveoak.container.LiveOakSystem;
import io.liveoak.spi.MediaType;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;

/**
 * @author Ken Finnigan
 */
public class ApplicationDeleteTest {


    private LiveOakSystem system;
    private CloseableHttpClient httpClient;
    private File appDir;

    @Before
    public void setUpServer() throws Exception {
        appDir = new File(getClass().getClassLoader().getResource("apps").getFile());
        this.system = LiveOakFactory.create(null, appDir, null);
        this.system.extensionInstaller().load("dummy", new InMemoryDBExtension());

        this.system.awaitStability();
    }

    @After
    public void tearDownServer() throws Exception {
        this.system.stop();
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

    @Test
    public void testAppDeleteAndRecreate() throws Throwable {
        this.system.applicationRegistry().createApplication("testApp1", "Test Application 1");
        this.system.awaitStability();

        HttpDelete deleteRequest;
        HttpPost postRequest;
        CloseableHttpResponse response;

        // Delete app
        deleteRequest = new HttpDelete("http://localhost:8080/admin/applications/testApp1");
        response = httpClient.execute(deleteRequest);

        assertThat(response).isNotNull();
        assertThat(response.getStatusLine().getStatusCode()).isEqualTo(200);

        this.system.awaitStability();
        response.close();

        // Recreate app with same name
        postRequest = new HttpPost("http://localhost:8080/admin/applications");
        postRequest.setEntity(new StringEntity("{ \"id\": \"testApp1\" }"));
        postRequest.setHeader("Content-Type", MediaType.JSON.toString());

        response = httpClient.execute(postRequest);

        assertThat(response).isNotNull();
        assertThat(response.getStatusLine().getStatusCode()).isEqualTo(201);
        response.close();

        // Delete app
        deleteRequest = new HttpDelete("http://localhost:8080/admin/applications/testApp1");
        response = httpClient.execute(deleteRequest);

        assertThat(response).isNotNull();
        assertThat(response.getStatusLine().getStatusCode()).isEqualTo(200);
        response.close();
    }

    @Test
    public void testAppDeleteAndRecreateWithResources() throws Throwable {
        InternalApplication app = this.system.applicationRegistry().createApplication("testApp2", "Test Application 2");
        app.extend("dummy");
        this.system.awaitStability();

        HttpDelete deleteRequest;
        HttpPost postRequest;
        CloseableHttpResponse response;

        // Delete app
        deleteRequest = new HttpDelete("http://localhost:8080/admin/applications/testApp2");
        response = httpClient.execute(deleteRequest);

        assertThat(response).isNotNull();
        assertThat(response.getStatusLine().getStatusCode()).isEqualTo(200);
        response.close();

        // Recreate app with same name
        postRequest = new HttpPost("http://localhost:8080/admin/applications");
        postRequest.setEntity(new StringEntity("{ \"id\": \"testApp2\" }"));
        postRequest.setHeader("Content-Type", MediaType.JSON.toString());

        response = httpClient.execute(postRequest);

        assertThat(response).isNotNull();
        assertThat(response.getStatusLine().getStatusCode()).isEqualTo(201);
        response.close();
        this.system.awaitStability();

        // Add Resource to new app
        postRequest = new HttpPost("http://localhost:8080/admin/applications/testApp2/resources");
        postRequest.setEntity(new StringEntity("{ \"id\": \"testApp2\", \"type\": \"dummy\" }"));
        postRequest.setHeader("Content-Type", MediaType.JSON.toString());

        assertThat(response).isNotNull();
        assertThat(response.getStatusLine().getStatusCode()).isEqualTo(201);
        response.close();
        this.system.awaitStability();

        // Delete app
        deleteRequest = new HttpDelete("http://localhost:8080/admin/applications/testApp2");
        response = httpClient.execute(deleteRequest);

        assertThat(response).isNotNull();
        assertThat(response.getStatusLine().getStatusCode()).isEqualTo(200);
        response.close();
    }
}
