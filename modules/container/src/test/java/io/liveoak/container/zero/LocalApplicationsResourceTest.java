package io.liveoak.container.zero;

import java.io.File;
import java.nio.charset.Charset;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import io.liveoak.container.AbstractContainerTest;
import io.liveoak.container.InMemoryDBExtension;
import io.liveoak.container.LiveOakFactory;
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
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.revwalk.RevCommit;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;

/**
 * @author Ken Finnigan
 */
public class LocalApplicationsResourceTest extends AbstractContainerTest {

    private CloseableHttpClient httpClient;
    private static File appDir;

    @BeforeClass
    public static void setUpServer() throws Exception {
        appDir = new File(LocalApplicationsResourceTest.class.getClassLoader().getResource("apps").getFile());
        system = LiveOakFactory.create(null, appDir, preWaitSetupConsumer());
        system.extensionInstaller().load("dummy", new InMemoryDBExtension());

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
        new AppCleanup().accept("apps/myapp");
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
        return system.codecManager().decode(MediaType.LOCAL_APP_JSON, buffer);
    }

    protected ResourceState decode(ByteBuf buffer) throws Exception {
        System.err.println("========= ByteBuf ==========");
        System.err.println(buffer.toString(Charset.defaultCharset()));
        System.err.println("===================");
        return system.codecManager().decode(MediaType.LOCAL_APP_JSON, buffer);
    }

    @Test
    public void localAppImportTests() throws Exception {
        HttpPost postRequest;
        CloseableHttpResponse response;

        // Test #1 - App Creation failure with no local path
        // Post an application
        postRequest = new HttpPost("http://localhost:8080/admin/applications");
        postRequest.setEntity(new StringEntity("{ \"id\": \"myapp\", \"name\": \"My Application\" }"));
        postRequest.setHeader("Content-Type", MediaType.LOCAL_APP_JSON.toString());

        response = httpClient.execute(postRequest);
        assertThat(response).isNotNull();
        assertThat(response.getStatusLine().getStatusCode()).isEqualTo(406);
        response.close();

        // Test #2 - App Creation failure with empty local path
        // Post an application
        postRequest = new HttpPost("http://localhost:8080/admin/applications");
        postRequest.setEntity(new StringEntity("{ \"id\": \"myapp\", \"name\": \"My Application\", \"localPath\": \"\" }"));
        postRequest.setHeader("Content-Type", MediaType.LOCAL_APP_JSON.toString());

        response = httpClient.execute(postRequest);
        assertThat(response).isNotNull();
        assertThat(response.getStatusLine().getStatusCode()).isEqualTo(406);
        response.close();

        // Test #3 - App Creation failure with bad local path
        // Post an application
        postRequest = new HttpPost("http://localhost:8080/admin/applications");
        postRequest.setEntity(new StringEntity("{ \"id\": \"myapp\", \"name\": \"My Application\", \"localPath\": \"myDodgyPath\" }"));
        postRequest.setHeader("Content-Type", MediaType.LOCAL_APP_JSON.toString());

        response = httpClient.execute(postRequest);
        assertThat(response).isNotNull();
        assertThat(response.getStatusLine().getStatusCode()).isEqualTo(406);
        response.close();

        // Test #4 - App Creation failure with non existent application json
        // Post an application
        String badAppPath = new File(getClass().getClassLoader().getResource("importApps/badApp").getFile()).getAbsolutePath();
        // make sure to escape special chars in order to compose valid JSON
        badAppPath = badAppPath.replace("\\", "\\\\");
        postRequest = new HttpPost("http://localhost:8080/admin/applications");
        postRequest.setEntity(new StringEntity("{ \"id\": \"badapp\", \"name\": \"Bad Application\", \"localPath\": \"" + badAppPath + "\" }"));
        postRequest.setHeader("Content-Type", MediaType.LOCAL_APP_JSON.toString());

        response = httpClient.execute(postRequest);
        assertThat(response).isNotNull();
        assertThat(response.getStatusLine().getStatusCode()).isEqualTo(406);
        response.close();

        // Test #5 - App Creation
        CompletableFuture<StompMessage> appCreationNotification = new CompletableFuture<>();

        StompClient stompClient = new StompClient();

        CountDownLatch subscriptionLatch = new CountDownLatch(1);

        stompClient.connect("localhost", 8080, (client) -> {
            stompClient.subscribe("/admin/applications/*", (subscription) -> {
                subscription.onMessage((msg) -> {
                    System.err.println("******* MESSAGE: " + msg);
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

        HttpGet getRequest;

        // Post an application
        String appPath = new File(getClass().getClassLoader().getResource("importApps/app1").getFile()).getAbsolutePath();
        // make sure to escape special chars in order to compose valid JSON
        appPath = appPath.replace("\\", "\\\\");

        postRequest = new HttpPost("http://localhost:8080/admin/applications");
        postRequest.setEntity(new StringEntity("{ \"id\": \"myapp\", \"localPath\": \"" + appPath + "\" }"));
        postRequest.setHeader("Content-Type", MediaType.LOCAL_APP_JSON.toString());

        response = httpClient.execute(postRequest);

        awaitStability();

        assertThat(response).isNotNull();
        assertThat(response.getStatusLine().getStatusCode()).isEqualTo(201);

        ResourceState state = decode(response);
        assertThat(state).isNotNull();
        assertThat(state).isInstanceOf(ResourceState.class);

        assertThat(state.id()).isNotNull();
        assertThat(state.id()).isEqualTo("myapp");
        assertThat(state.getProperty(LiveOak.NAME)).isEqualTo("app1");
        assertThat(state.getProperty("visible")).isEqualTo(true);

        // check STOMP
        StompMessage obj = appCreationNotification.get(30, TimeUnit.SECONDS);
        assertThat(obj).isNotNull();

        ResourceState appObjState = decode(obj.content());
        assertThat(appObjState.getProperty(LiveOak.NAME)).isEqualTo("app1");

        assertThat(state.getProperty(LiveOak.ID)).isEqualTo(appObjState.getProperty(LiveOak.ID));
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
        assertThat(system.vertx().fileSystem().existsSync(appDir.getPath() + "/myapp/app/index.html")).isTrue();

        // Check git present
        File myApp = new File(appDir, "myapp");
        assertThat(new File(myApp, ".git").exists()).isTrue();
        Git git = Git.open(myApp);
        assertThat(git.status().call().hasUncommittedChanges()).isFalse();
        Iterable<RevCommit> commits = git.log().call();
        int count = 0;
        for (RevCommit rev : commits) {
            count++;
        }
        assertThat(count).isEqualTo(1);

        // Test #6 - Check that READ is unsupported
        getRequest = new HttpGet("http://localhost:8080/admin/applications/members");
        getRequest.setHeader("Accept", MediaType.LOCAL_APP_JSON.toString());

        response = httpClient.execute(getRequest);
        assertThat(response).isNotNull();
        assertThat(response.getStatusLine().getStatusCode()).isEqualTo(405);
        response.close();

        // Test #7 - Check that UPDATE is unsupported
        HttpPut putRequest = new HttpPut("http://localhost:8080/admin/applications");
        putRequest.setHeader("Content-Type", MediaType.LOCAL_APP_JSON.toString());

        response = httpClient.execute(putRequest);
        assertThat(response).isNotNull();
        assertThat(response.getStatusLine().getStatusCode()).isEqualTo(405);
        response.close();

        // Test #8 - Check that DELETE is unsupported
        HttpDelete deleteRequest = new HttpDelete("http://localhost:8080/admin/applications");
        getRequest.setHeader("Accept", MediaType.LOCAL_APP_JSON.toString());

        response = httpClient.execute(deleteRequest);
        assertThat(response).isNotNull();
        assertThat(response.getStatusLine().getStatusCode()).isEqualTo(405);
        response.close();
    }
}
