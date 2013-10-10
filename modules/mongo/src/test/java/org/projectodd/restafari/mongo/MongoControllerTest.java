package org.projectodd.restafari.mongo;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import io.netty.channel.nio.NioEventLoopGroup;
import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.*;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.projectodd.restafari.container.Container;
import org.projectodd.restafari.container.SimpleConfig;
import org.projectodd.restafari.container.UnsecureServer;

import java.io.ByteArrayOutputStream;
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
        String port = "27018"; //TODO: load this through a system property instead of hardcoding it here
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

@Test
    public void testSimpleCreate() throws Exception {
        createSimpleItem("http://localhost:8080/storage/items", "{\"foo\":\"bar\"}");
    }

    @Test
    public void testSimpleGet() throws Exception {
        String id = createSimpleItem("http://localhost:8080/storage/items", "{\"foo\":\"bar\"}");

        String result = getSimpleItem("http://localhost:8080/storage/items", id);

        String expected = "{\"id\":\"" + id + "\",\"foo\":\"bar\"}";
        assertEquals(expected, result);
    }

    @Test
    public void testSimpleDelete() throws Exception {

        String id = createSimpleItem("http://localhost:8080/storage/items", "{\"foo\":\"bar\"}");

        //test that we can get this newly created object
        CloseableHttpResponse getResponse = createSimpleGet("http://localhost:8080/storage/items" + "/" + id);
        assertEquals(200, getResponse.getStatusLine().getStatusCode());

        CloseableHttpResponse deleteResponse = createSimpleDelete("http://localhost:8080/storage/items" + "/" + id);
        assertEquals(200, deleteResponse.getStatusLine().getStatusCode());

        //test that we now get a 404 when trying to access this object after its deleted
        getResponse = createSimpleGet("http://localhost:8080/storage/items" + "/" + id);
        assertEquals(404, getResponse.getStatusLine().getStatusCode());
    }

    @Test
    public void testSimpleUpdate() throws Exception {

        String id = createSimpleItem("http://localhost:8080/storage/items", "{\"foo\":\"bar\"}");

        //test that we can get this newly created object
        String result = getSimpleItem("http://localhost:8080/storage/items", id);
        String expected = "{\"id\":\"" + id + "\",\"foo\":\"bar\"}";
        assertEquals(expected, result);

        CloseableHttpResponse updateResponse = createSimplePut("http://localhost:8080/storage/items" + "/" + id, "{ \"foo\": \"baz\" }");

        //test that we get back now an updated object
        result = getSimpleItem("http://localhost:8080/storage/items", id);
        expected = "{\"id\":\"" + id + "\",\"foo\":\"baz\"}";
        assertEquals(expected, result);
    }

    public String createSimpleItem(String url, String content) throws Exception {
        CloseableHttpResponse response = createSimplePost("http://localhost:8080/storage/items", content);
        assertEquals(201, response.getStatusLine().getStatusCode());

        //TODO: test that the location header exists when this feature is added
        //assertNotNull(response.getFirstHeader("Location"));

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        response.getEntity().writeTo(outputStream);
        String result = new String(outputStream.toByteArray());
        outputStream.close();

        String id = getId(result.getBytes());
        assertNotNull("Could not find the id for the created resource", id);

        String expected = "{\"id\":\"" + id + "\"," + content.substring(1); // substring(1) to remove the staring {
        assertEquals(expected, result);
        return id;
    }

    public String getSimpleItem(String url, String id) throws Exception {
        CloseableHttpResponse getResponse = createSimpleGet(url + "/" + id);
        assertEquals(200, getResponse.getStatusLine().getStatusCode());

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        getResponse.getEntity().writeTo(outputStream);
        String result = new String(outputStream.toByteArray());
        outputStream.close();

        return result;
    }

    public String getId(byte[] entity) throws Exception{
        JsonFactory factory = new JsonFactory();
        JsonParser parser = factory.createParser(entity);

        String id = null;
        while (parser.nextToken() != JsonToken.END_OBJECT)
        {
            String name = parser.getCurrentName();
            if (name !=null && name.equals("id"))
            {
                parser.nextToken();
                id = parser.getValueAsString();
                break;
            }
        }
        if (id == null)
        {
            fail("Could not find ID from the response");
        }

        return id;
    }

    public CloseableHttpResponse createSimpleGet(String url) throws Exception {
        HttpGet get = new HttpGet(url);
        get.addHeader("Accept", "application/json");

        CloseableHttpClient httpClient = HttpClientBuilder.create().build();
        return httpClient.execute(get);
    }

    public CloseableHttpResponse createSimpleDelete(String url) throws Exception {
        HttpDelete delete = new HttpDelete(url);
        delete.addHeader("Accept", "application/json");

        CloseableHttpClient httpClient = HttpClientBuilder.create().build();
        return httpClient.execute(delete);
    }

    public CloseableHttpResponse createSimplePost(String url, String body) throws Exception {
        return createSimplePost(url, "application/json", body);
    }

    public CloseableHttpResponse createSimplePost(String url, String acceptHeader, String body) throws Exception{
        HttpPost post = new HttpPost(url);
        post.addHeader("Accept", acceptHeader);

        StringEntity entity = new StringEntity(body, ContentType.create("text/plain", "UTF-8"));

        post.setEntity(entity);

        CloseableHttpClient httpClient = HttpClientBuilder.create().build();
        return httpClient.execute(post);
    }

    public CloseableHttpResponse createSimplePut(String url, String body) throws Exception {
        return createSimplePut(url, "application/json", body);
    }

    public CloseableHttpResponse createSimplePut(String url, String acceptHeader, String body) throws Exception{
        HttpPut put = new HttpPut(url);
        put.addHeader("Accept", acceptHeader);

        StringEntity entity = new StringEntity(body, ContentType.create("text/plain", "UTF-8"));

        put.setEntity(entity);

        CloseableHttpClient httpClient = HttpClientBuilder.create().build();
        return httpClient.execute(put);
    }
}
