package org.projectodd.restafari.deployer;

import io.netty.channel.nio.NioEventLoopGroup;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.projectodd.restafari.container.Container;
import org.projectodd.restafari.container.SimpleConfig;
import org.projectodd.restafari.container.UnsecureServer;

import java.io.IOException;
import java.net.InetAddress;

import static org.junit.Assert.*;

/**
 * @author <a href="mailto:lball@redhat.com">Lance Ball</a>
 */
public class DeployerControllerTest {

    private static SimpleConfig config;
    private static UnsecureServer server;
    private static CloseableHttpClient client;

    @BeforeClass
    public static void init() {
        config = new SimpleConfig();
        config.put("mbaas.deployer.dir", System.getProperty("user.dir"));
        Container container = new Container();
        try {
            container.registerResourceController("deployer", new DeployerController(), config);
            server = new UnsecureServer(container, InetAddress.getByName("localhost"), 8080, new NioEventLoopGroup());
            server.start();
        } catch (Exception e) {
            fail("Can't initialize container for testing");
        }
        client = HttpClientBuilder.create().build();
    }

    @AfterClass
    public static void tearDown() throws InterruptedException, IOException {
        client.close();
        server.stop();
    }

    @Test
    public void testDeployerControllerGetDeployments() {

        HttpGet get = new HttpGet("http://localhost:8080/deployer/deployments");
        get.addHeader("Accept", "application/json");
        CloseableHttpResponse result = null;
        try {
            result = client.execute(get);
        } catch (IOException e) {
            fail("Can't get deployments");
        }
        assertEquals(200, result.getStatusLine().getStatusCode());
    }
}
