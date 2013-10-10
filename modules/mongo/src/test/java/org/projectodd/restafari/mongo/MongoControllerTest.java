package org.projectodd.restafari.mongo;

import io.netty.channel.nio.NioEventLoopGroup;
import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.projectodd.restafari.container.Container;
import org.projectodd.restafari.container.SimpleConfig;
import org.projectodd.restafari.container.UnsecureServer;

import java.net.InetAddress;

import static org.junit.Assert.*;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 */
public class MongoControllerTest {

    private static SimpleConfig config;
    private static UnsecureServer server;

    @BeforeClass
    public static void init() throws Exception {
        config = new SimpleConfig();
        config.put("db", System.getProperty("mongo.db"));
        config.put("host", System.getProperty("mongo.host"));
        String port = System.getProperty("mongo.port");
        if (port != null) {
            config.put("port", new Integer(port));
        }

        if (config.get("db", null) != null) {
            Container container = new Container();
            container.registerResourceController("storage", new MongoController(), config);

            server = new UnsecureServer(container, InetAddress.getByName("localhost"), 8080, new NioEventLoopGroup());

            System.err.println("START SERVER");
            server.start();
            System.err.println("STARTED SERVER");
        } else {
            System.err.println("No mongo.db configuration. Skipping tests ...");
        }
    }

    @AfterClass
    public static void dispose() {
        if (server == null)
            return;

        try {
            server.stop();
        } catch (InterruptedException ignored) {
        }
    }

    @Test
    public void testNotFound() throws Exception {
        if (server == null)
            return;

        RequestConfig cconfig = RequestConfig.custom().setSocketTimeout(500000).build();
        CloseableHttpClient httpClient = HttpClients.custom().setDefaultRequestConfig(cconfig).build();

        HttpGet get = new HttpGet("http://localhost:8080/storage");
        get.addHeader("Accept", "application/json");

        try {
            System.err.println("DO GET");
            CloseableHttpResponse result = httpClient.execute(get);
            System.err.println("=============>>>");
            System.err.println(result);

            HttpEntity entity = result.getEntity();
            if (entity.getContentLength() > 0) {
                entity.writeTo(System.err);
            }
            System.err.println("\n<<<=============");
            assertEquals(404, result.getStatusLine().getStatusCode());

        } finally {
            httpClient.close();
        }
    }

    @Test
    public void testAutocreateCollection() throws Exception {

        if (server == null)
            return;

        CloseableHttpClient httpClient = HttpClientBuilder.create().build();
        HttpGet get = new HttpGet("http://localhost:8080/storage/movies");
        get.addHeader("Accept", "application/json");

        try {
            System.err.println("DO GET");
            CloseableHttpResponse result = httpClient.execute(get);
            System.err.println("=============>>>");
            System.err.println(result);
            result.getEntity().writeTo(System.err);
            System.err.println("\n<<<=============");
            assertEquals(200, result.getStatusLine().getStatusCode());

        } finally {
            httpClient.close();
        }
    }
}
