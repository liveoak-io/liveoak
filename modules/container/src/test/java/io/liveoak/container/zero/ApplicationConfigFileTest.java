package io.liveoak.container.zero;

import io.liveoak.common.util.FileHelper;
import io.liveoak.container.AbstractContainerTest;
import io.liveoak.container.InMemoryDBExtension;
import io.liveoak.container.LiveOakFactory;
import io.liveoak.spi.MediaType;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import static org.fest.assertions.Assertions.assertThat;

/**
 * Created by mwringe on 20/02/15.
 */
public class ApplicationConfigFileTest extends AbstractContainerTest {
    private CloseableHttpClient httpClient;
    private static File appsDir;

    @BeforeClass
    public static void setUpServer() throws Exception {
        appsDir = new File(ApplicationConfigFileTest.class.getClassLoader().getResource("apps").getFile());

        File appDir = new File(appsDir, "capp1");
        appDir.mkdir();

        File configFileApp = new File(ApplicationConfigFileTest.class.getClassLoader().getResource("configFiles/capp1").getFile());
        FileHelper.copy(configFileApp, appDir, true, path -> false);

        system = LiveOakFactory.create(null, appsDir, null);
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
        new AppCleanup().accept("apps/capp1");
        new AppCleanup().accept("apps/capp2");
    }

    @Test
    public void testApp() throws Exception {
        File configFile = new File(appsDir, "capp1/conf/test");
        BufferedReader reader = new BufferedReader(new FileReader(configFile));
        assertThat(reader.readLine()).isEqualTo("Hello capp1");
        reader.close();

        File originalConfigFile = new File(appsDir, "capp1/conf/.test.config.orig");
        reader = new BufferedReader(new FileReader(originalConfigFile));
        assertThat(reader.readLine()).isEqualTo("Hello ${application.id}");
    }

    @Test
    public void testAppImport() throws Exception {
        HttpPost postRequest;
        CloseableHttpResponse response;

        String capp1Path = new File(appsDir, "capp1").getPath();
        File configFile = new File(appsDir, "capp1/conf/test");
        BufferedReader reader = new BufferedReader(new FileReader(configFile));
        assertThat(reader.readLine()).isEqualTo("Hello capp1");
        reader.close();

        postRequest = new HttpPost("http://localhost:8080/admin/applications");
        postRequest.setEntity(new StringEntity("{ \"id\": \"capp2\", \"localPath\": \"" + capp1Path + "\" }"));
        postRequest.setHeader("Content-Type", MediaType.LOCAL_APP_JSON.toString());

        response = httpClient.execute(postRequest);
        assertThat(response).isNotNull();
        assertThat(response.getStatusLine().getStatusCode()).isEqualTo(201);
        response.close();

        File capp2ConfigFile = new File(appsDir, "capp2/conf/test");
        reader = new BufferedReader(new FileReader(capp2ConfigFile));
        assertThat(reader.readLine()).isEqualTo("Hello capp2");
        reader.close();
    }

}
