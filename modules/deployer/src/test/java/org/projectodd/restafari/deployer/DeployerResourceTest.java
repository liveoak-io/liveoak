package org.projectodd.restafari.deployer;

import org.apache.http.impl.client.CloseableHttpClient;
import org.projectodd.restafari.container.SimpleConfig;
import org.projectodd.restafari.container.UnsecureServer;

/**
 * @author <a href="mailto:lball@redhat.com">Lance Ball</a>
 */
public class DeployerResourceTest {

    private static SimpleConfig config;
    private static UnsecureServer server;
    private static CloseableHttpClient client;

    /*
    @BeforeClass
    public static void init() {
        config = new SimpleConfig();
        config.put("mbaas.deployer.dir", System.getProperty("user.dir"));
        DefaultContainer container = new DefaultContainer();
        try {
            container.registerResourceController("deployer", new DeployerResource(), config);
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
            fail("Can't writeState deployments");
        }
        assertEquals(200, result.getStatusLine().getStatusCode());
    }
    */
}
