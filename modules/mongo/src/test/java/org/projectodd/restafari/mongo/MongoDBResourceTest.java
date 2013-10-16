package org.projectodd.restafari.mongo;

import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.projectodd.restafari.container.DefaultContainer;
import org.projectodd.restafari.container.SimpleConfig;
import org.projectodd.restafari.container.UnsecureServer;

import static org.fest.assertions.Assertions.assertThat;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 */
public class MongoDBResourceTest {

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
            DefaultContainer container = new DefaultContainer();
            container.registerResource(new MongoDBResource("storage"), config);

            server = new UnsecureServer(container, "localhost", 8080);

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
    public void testRootFound() throws Exception {
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
            assertThat(result.getStatusLine().getStatusCode()).isEqualTo(200);

        } finally {
            httpClient.close();
        }
    }

    @Test
    public void testUncreatedCollectionNotFound() throws Exception {

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
            assertThat(result.getStatusLine().getStatusCode()).isEqualTo(404);

        } finally {
            httpClient.close();
        }
    }

    @Test
    public void testCreateCollection() throws Exception {

        if (server == null)
            return;

        CloseableHttpClient httpClient = HttpClientBuilder.create().build();

        HttpPut put = new HttpPut("http://localhost:8080/storage/movies");
        put.setEntity(new StringEntity("{ \"id\": \"movies\", \"members\": [] }"));

        CloseableHttpResponse result = httpClient.execute(put);
        System.err.println("=============>>>");
        System.err.println(result);
        result.getEntity().writeTo(System.err);
        System.err.println("\n<<<=============");
        assertThat(result.getStatusLine().getStatusCode()).isEqualTo(201);


        HttpGet get = new HttpGet("http://localhost:8080/storage/movies");
        get.addHeader("Accept", "application/json");

        try {
            System.err.println("DO GET");
            result = httpClient.execute(get);
            System.err.println("=============>>>");
            System.err.println(result);
            result.getEntity().writeTo(System.err);
            System.err.println("\n<<<=============");
            assertThat(result.getStatusLine().getStatusCode()).isEqualTo(404);

        } finally {
            httpClient.close();
        }
    }
}
