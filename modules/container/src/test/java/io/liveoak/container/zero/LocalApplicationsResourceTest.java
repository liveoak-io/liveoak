package io.liveoak.container.zero;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import io.liveoak.container.LiveOakFactory;
import io.liveoak.container.LiveOakSystem;
import io.liveoak.spi.MediaType;
import io.liveoak.spi.state.ResourceState;
import io.liveoak.stomp.StompMessage;
import io.liveoak.stomp.client.StompClient;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.buffer.Unpooled;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
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
public class LocalApplicationsResourceTest {

    private LiveOakSystem system;

    protected CloseableHttpClient httpClient;

    @Before
    public void setUpServer() throws Exception {
        File appDir = new File(getClass().getClassLoader().getResource("apps").getFile());
        this.system = LiveOakFactory.create(null, appDir, null);

        this.system.awaitStability();
    }

    @Before
    public void setUpClient() throws Exception {
        this.httpClient = HttpClientBuilder.create().build();
    }

    @After
    public void tearDownClient() throws Exception {
        this.httpClient.close();
    }

    @After
    public void tearDownServer() throws Exception {
        this.system.stop();
        System.err.flush();
    }

    @After
    public void cleanUpInstalledApps() throws Exception {
        File myApp = new File(getClass().getClassLoader().getResource("apps/myapp").getFile());
        if (myApp != null && myApp.exists()) {
            deleteNonEmptyDir(myApp);
        }
        File resApp = new File(getClass().getClassLoader().getResource("apps/resapp").getFile());
        if (resApp != null && resApp.exists()) {
            deleteNonEmptyDir(resApp);
        }
    }

    private void deleteNonEmptyDir(File dir) throws IOException {
        Path directory = dir.toPath();
        Files.walkFileTree(directory, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.delete(file);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                Files.delete(dir);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    protected ResourceState decode(HttpResponse response) throws Exception {
        ByteBuf buffer = Unpooled.buffer();
        ByteBufOutputStream out = new ByteBufOutputStream(buffer);
        response.getEntity().writeTo(out);
        out.flush();
        out.close();
        System.err.println("========= HttpResponse ==========");
        System.err.println(buffer.toString(Charset.defaultCharset()));
        System.err.println("===================");
        return this.system.codecManager().decode(MediaType.LOCAL_APP_JSON, buffer);
    }

    protected ResourceState decode(ByteBuf buffer) throws Exception {
        System.err.println("========= ByteBuf ==========");
        System.err.println(buffer.toString(Charset.defaultCharset()));
        System.err.println("===================");
        return this.system.codecManager().decode(MediaType.LOCAL_APP_JSON, buffer);
    }

    @Test
    public void testAppCreationFromLocalPath() throws Exception {
        CompletableFuture<StompMessage> appCreationNotification = new CompletableFuture<>();

        StompClient stompClient = new StompClient();

        CountDownLatch subscriptionLatch = new CountDownLatch(1);

        stompClient.connect("localhost", 8080, (client) -> {
            stompClient.subscribe("/admin/applications/*", (subscription) -> {
                subscription.onMessage((msg) -> {
                    System.err.println( "******* MESSAGE: "+ msg );
                    if (msg.headers().get("location").equals("/admin/applications/myapp")) {
                        appCreationNotification.complete(msg);
                    }
                });
                subscription.onReceipt(() -> {
                    subscriptionLatch.countDown();
                });
            });
        });

        subscriptionLatch.await();

        HttpPost postRequest = null;
        HttpGet getRequest = null;
        CloseableHttpResponse response = null;

        // Post an application
        File app1LocalPath = new File(getClass().getClassLoader().getResource("importApps/app1").getFile());
        postRequest = new HttpPost("http://localhost:8080/admin/applications");
        postRequest.setEntity(new StringEntity("{ \"id\": \"myapp\", \"name\": \"My Application\", \"localPath\": \"" + app1LocalPath.getAbsolutePath() + "\" }"));
        postRequest.setHeader("Content-Type", MediaType.LOCAL_APP_JSON.toString());

        response = httpClient.execute(postRequest);
        assertThat(response).isNotNull();
        assertThat(response.getStatusLine().getStatusCode()).isEqualTo(201);

        ResourceState state = decode(response);
        assertThat(state).isNotNull();
        assertThat(state).isInstanceOf(ResourceState.class);

        assertThat(state.id()).isNotNull();
        assertThat(state.getProperty("id")).isEqualTo("myapp");
        assertThat(state.getProperty("name")).isEqualTo("My Application");
        assertThat(state.getProperty("visible")).isEqualTo(true);

        // check STOMP
        StompMessage obj = appCreationNotification.get(30000, TimeUnit.SECONDS);
        assertThat(obj).isNotNull();

        ResourceState appObjState = decode(obj.content());
        assertThat(appObjState.getProperty("name")).isEqualTo("My Application");

        assertThat(state.getProperty("id")).isEqualTo(appObjState.getProperty("id"));
        response.close();

        // Check resources are present
        getRequest = new HttpGet("http://localhost:8080/myapp?expand=members");
        getRequest.setHeader("Content-Type", MediaType.JSON.toString());

        response = this.httpClient.execute(getRequest);

        assertThat(response).isNotNull();
        assertThat(response.getStatusLine().getStatusCode()).isEqualTo(200);

        state = decode(response);
        assertThat(state).isNotNull();
        assertThat(state.members().size()).isEqualTo(3);

        response.close();

        // Check files were copied across
        getRequest = new HttpGet("http://localhost:8080/myapp/resources/app");
        getRequest.setHeader("Content-Type", MediaType.JSON.toString());

        response = this.httpClient.execute(getRequest);

        assertThat(response).isNotNull();
        assertThat(response.getStatusLine().getStatusCode()).isEqualTo(200);

        state = decode(response);
        assertThat(state).isNotNull();
        assertThat(state.members().get(0).id()).isEqualTo("index.html");
    }

    @Test
    public void testAppCreationFromLocalPathWithResourceOverwrite() throws Exception {
        HttpPost postRequest = null;
        HttpGet getRequest = null;
        CloseableHttpResponse response = null;

        // Post an application
        File app1LocalPath = new File(getClass().getClassLoader().getResource("importApps/resourcesApp").getFile());
        postRequest = new HttpPost("http://localhost:8080/admin/applications");
        postRequest.setEntity(new StringEntity("{ \"id\": \"resapp\", \"name\": \"Resource Application\", \"localPath\": \"" + app1LocalPath.getAbsolutePath() + "\","
            + "\"resources\": { \"code\": { \"type\": \"filesystem\", \"config\": { \"directory\": \"${application.dir}/app/\" } } } }"));
        postRequest.setHeader("Content-Type", MediaType.LOCAL_APP_JSON.toString());

        response = httpClient.execute(postRequest);
        assertThat(response).isNotNull();
        assertThat(response.getStatusLine().getStatusCode()).isEqualTo(201);

        ResourceState state = decode(response);
        assertThat(state).isNotNull();
        assertThat(state).isInstanceOf(ResourceState.class);

        assertThat(state.id()).isNotNull();
        assertThat(state.getProperty("id")).isEqualTo("resapp");
        assertThat(state.getProperty("name")).isEqualTo("Resource Application");
        assertThat(state.getProperty("visible")).isEqualTo(true);

        // Check resources were overwritten
        getRequest = new HttpGet("http://localhost:8080/resapp?expand=members");
        getRequest.setHeader("Content-Type", MediaType.JSON.toString());

        response = this.httpClient.execute(getRequest);

        assertThat(response).isNotNull();
        assertThat(response.getStatusLine().getStatusCode()).isEqualTo(200);

        state = decode(response);
        assertThat(state).isNotNull();
        assertThat(state.members().size()).isEqualTo(2);
        assertThat(state.members().get(0).id()).isEqualTo("code");

        response.close();

        // Check files were copied across
        getRequest = new HttpGet("http://localhost:8080/resapp/resources/code");
        getRequest.setHeader("Content-Type", MediaType.JSON.toString());

        response = this.httpClient.execute(getRequest);

        assertThat(response).isNotNull();
        assertThat(response.getStatusLine().getStatusCode()).isEqualTo(200);

        state = decode(response);
        assertThat(state).isNotNull();
        assertThat(state.members().get(0).id()).isEqualTo("index.html");
    }
}
