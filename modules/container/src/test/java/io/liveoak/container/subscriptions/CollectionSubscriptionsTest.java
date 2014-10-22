package io.liveoak.container.subscriptions;

import java.io.File;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import io.liveoak.container.LiveOakFactory;
import io.liveoak.container.LiveOakSystem;
import io.liveoak.container.zero.AppCleanup;
import io.liveoak.spi.MediaType;
import io.liveoak.stomp.StompMessage;
import io.liveoak.stomp.client.StompClient;
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
public class CollectionSubscriptionsTest {

    private LiveOakSystem system;
    private File appDir;

    protected CloseableHttpClient httpClient;

    @Before
    public void setUpServer() throws Exception {
        appDir = new File(getClass().getClassLoader().getResource("apps").getFile());
        this.system = LiveOakFactory.create(null, appDir, null);
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

    @AfterClass
    public static void cleanUpInstalledApps() throws Exception {
        new AppCleanup().accept("apps/myapp");
    }

    @Test
    public void notificationOnAppCreate() throws Exception {
        CompletableFuture<StompMessage> appCreationNotification = new CompletableFuture<>();
        CompletableFuture<StompMessage> wildcardNotification = new CompletableFuture<>();
        AtomicInteger appCreateCount = new AtomicInteger();
        AtomicInteger wildcardCount = new AtomicInteger();
        StompClient stompClient = new StompClient();

        CountDownLatch subscriptionLatch = new CountDownLatch(2);

        stompClient.connect("localhost", 8080, (client) -> {
            stompClient.subscribe("/admin/applications", (subscription) -> {
                subscription.onMessage((msg) -> {
                    System.err.println( "******* /admin/applications MESSAGE: " + msg);
                    appCreateCount.getAndIncrement();
                    appCreationNotification.complete(msg);
                });
                subscription.onReceipt(() -> subscriptionLatch.countDown());
            });
            stompClient.subscribe("/admin/applications/*", (subscription) -> {
                subscription.onMessage((msg) -> {
                    System.err.println( "******* /admin/applications/* MESSAGE: " + msg);
                    wildcardCount.getAndIncrement();
                    wildcardNotification.complete(msg);
                });
                subscription.onReceipt(() -> subscriptionLatch.countDown());
            });
        });

        subscriptionLatch.await();

        assertThat(appCreationNotification.getNow(null)).isNull();

        HttpPost postRequest;
        CloseableHttpResponse response;

        // Post an application
        postRequest = new HttpPost("http://localhost:8080/admin/applications");
        postRequest.setEntity(new StringEntity("{ \"id\": \"myapp\", \"name\": \"My Application\" }"));
        postRequest.setHeader("Content-Type", MediaType.JSON.toString());

        response = httpClient.execute(postRequest);
        assertThat(response).isNotNull();
        assertThat(response.getStatusLine().getStatusCode()).isEqualTo(201);

        response.close();

        this.system.awaitStability();

        // Give it time to propagate
        Thread.sleep(1500);

        assertThat(appCreateCount.get()).isEqualTo(1);
        assertThat(wildcardCount.get()).isEqualTo(1);

        StompMessage msg = appCreationNotification.get(30, TimeUnit.SECONDS);
        assertThat(msg).isNotNull();
        assertThat(msg.headers().get("action")).isEqualTo("create");
        assertThat(msg.headers().get("location")).isEqualTo("/admin/applications/myapp");

        msg = wildcardNotification.get(30, TimeUnit.SECONDS);
        assertThat(msg).isNotNull();
        assertThat(msg.headers().get("action")).isEqualTo("create");
        assertThat(msg.headers().get("location")).isEqualTo("/admin/applications/myapp");
    }
}
