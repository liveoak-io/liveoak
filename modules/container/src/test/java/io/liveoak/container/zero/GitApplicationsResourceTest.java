package io.liveoak.container.zero;

import java.io.File;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import io.liveoak.container.AbstractContainerTest;
import io.liveoak.container.LiveOakFactory;
import io.liveoak.container.extension.MockExtension;
import io.liveoak.spi.LiveOak;
import io.liveoak.spi.MediaType;
import io.liveoak.spi.state.ResourceState;
import io.liveoak.stomp.StompMessage;
import io.liveoak.stomp.client.StompClient;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.buffer.Unpooled;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;

/**
 * @author Ken Finnigan
 */
public class GitApplicationsResourceTest extends AbstractContainerTest {
    private CloseableHttpClient httpClient;
    private File appsDir;

    @Before
    public void setUpServer() throws Exception {
        appsDir = new File(getClass().getClassLoader().getResource("apps").getFile());
        this.system = LiveOakFactory.create(null, appsDir, null);
        this.system.extensionInstaller().load("aggregating-filesystem", new MockExtension("aggr-filesystem"));
        this.system.extensionInstaller().load("filesystem", new MockExtension("filesystem"));
        this.system.extensionInstaller().load("mongo", new MockExtension("mongo"));

        awaitStability();
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

    @AfterClass
    public static void cleanUpInstalledApps() throws Exception {
        new AppCleanup().accept("apps/liveoak-example-chat-html");
        new AppCleanup().accept("apps/chat-html");
        new AppCleanup().accept("apps/gitapp");
    }

    protected ResourceState decode(HttpResponse response) throws Exception {
        ByteBuf buffer = Unpooled.buffer();
        ByteBufOutputStream out = new ByteBufOutputStream(buffer);
        response.getEntity().writeTo(out);
        out.flush();
        out.close();
        return this.system.codecManager().decode(MediaType.GIT_APP_JSON, buffer);
    }

    protected ResourceState decode(ByteBuf buffer) throws Exception {
        return this.system.codecManager().decode(MediaType.GIT_APP_JSON, buffer);
    }

    @Test
    public void gitAppImportTests() throws Exception {
        HttpPost postRequest;
        CloseableHttpResponse response;

        // Test #1 - App Creation failure with remote url not present
        postRequest = new HttpPost("http://localhost:8080/admin/applications");
        postRequest.setEntity(new StringEntity("{}"));
        postRequest.setHeader("Content-Type", MediaType.GIT_APP_JSON.toString());

        response = httpClient.execute(postRequest);
        assertThat(response).isNotNull();
        assertThat(response.getStatusLine().getStatusCode()).isEqualTo(406);
        response.close();


        // Test #2 - App Creation failure with remote url empty
        postRequest = new HttpPost("http://localhost:8080/admin/applications");
        postRequest.setEntity(new StringEntity("{ \"url\": \"\" }"));
        postRequest.setHeader("Content-Type", MediaType.GIT_APP_JSON.toString());

        response = httpClient.execute(postRequest);
        assertThat(response).isNotNull();
        assertThat(response.getStatusLine().getStatusCode()).isEqualTo(406);
        response.close();


        // Test #3 - App Creation failure with remote url invalid
        postRequest = new HttpPost("http://localhost:8080/admin/applications");
        postRequest.setEntity(new StringEntity("{ \"url\": \"http://localhost/gitapp.git\" }"));
        postRequest.setHeader("Content-Type", MediaType.GIT_APP_JSON.toString());

        response = httpClient.execute(postRequest);
        assertThat(response).isNotNull();
        assertThat(response.getStatusLine().getStatusCode()).isEqualTo(406);
        response.close();


        // Test #4 - App Creation with HTTP url
        CompletableFuture<StompMessage> appCreationNotification = new CompletableFuture<>();
        StompClient stompClient = new StompClient();
        CountDownLatch subscriptionLatch = new CountDownLatch(1);

        stompClient.connect("localhost", 8080, (client) -> {
            stompClient.subscribe("/admin/applications/*", (subscription) -> {
                subscription.onMessage((msg) -> {
                    System.err.println( "******* MESSAGE: "+ msg );
                    if (msg.headers().get("location").equals("/admin/applications/liveoak-example-chat-html")) {
                        appCreationNotification.complete(msg);
                    }
                });
                subscription.onReceipt(() -> {
                    subscriptionLatch.countDown();
                });
            });
        });

        subscriptionLatch.await();
        postRequest = new HttpPost("http://localhost:8080/admin/applications");
        postRequest.setEntity(new StringEntity("{ \"url\": \"https://github.com/liveoak-io/liveoak-example-chat-html.git\" }"));
        postRequest.setHeader("Content-Type", MediaType.GIT_APP_JSON.toString());

        response = httpClient.execute(postRequest);
        awaitStability();

        assertThat(response).isNotNull();
        assertThat(response.getStatusLine().getStatusCode()).isEqualTo(201);

        ResourceState state = decode(response);
        assertThat(state).isNotNull();
        assertThat(state).isInstanceOf(ResourceState.class);

        assertThat(state.id()).isNotNull();
        assertThat(state.id()).isEqualTo("liveoak-example-chat-html");
        assertThat(state.getProperty(LiveOak.NAME)).isEqualTo("liveoak-example-chat-html");
        assertThat(state.getProperty("visible")).isEqualTo(true);

        // check STOMP
        StompMessage obj = appCreationNotification.get(30, TimeUnit.SECONDS);
        assertThat(obj).isNotNull();

        ResourceState appObjState = decode(obj.content());
        assertThat(appObjState.getProperty(LiveOak.NAME)).isEqualTo("liveoak-example-chat-html");

        assertThat(state.getProperty(LiveOak.ID)).isEqualTo(appObjState.getProperty(LiveOak.ID));
        response.close();

        // Check git present
        File myApp = new File(appsDir, "liveoak-example-chat-html");
        assertThat(new File(myApp, ".git").exists()).isTrue();


        // Test #5 - App Creation with SSH url and setting id
        postRequest = new HttpPost("http://localhost:8080/admin/applications");
        postRequest.setEntity(new StringEntity("{ \"id\": \"chat-html\", \"url\": \"https://github.com/liveoak-io/liveoak-example-chat-html.git\" }"));
        postRequest.setHeader("Content-Type", MediaType.GIT_APP_JSON.toString());

        response = httpClient.execute(postRequest);
        awaitStability();

        assertThat(response).isNotNull();
        assertThat(response.getStatusLine().getStatusCode()).isEqualTo(201);

        state = decode(response);
        assertThat(state).isNotNull();
        assertThat(state).isInstanceOf(ResourceState.class);

        assertThat(state.id()).isNotNull();
        assertThat(state.id()).isEqualTo("chat-html");
        assertThat(state.getProperty(LiveOak.NAME)).isEqualTo("chat-html");
        assertThat(state.getProperty("visible")).isEqualTo(true);

        // Check git present
        myApp = new File(appsDir, "chat-html");
        assertThat(new File(myApp, ".git").exists()).isTrue();
    }
}
