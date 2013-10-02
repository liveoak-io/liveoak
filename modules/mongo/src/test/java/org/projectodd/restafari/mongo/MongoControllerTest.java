package org.projectodd.restafari.mongo;

import io.netty.channel.nio.NioEventLoopGroup;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.Test;
import org.projectodd.restafari.container.Container;
import org.projectodd.restafari.container.SimpleConfig;
import org.projectodd.restafari.container.UnsecureServer;

import java.net.InetAddress;
import java.util.Collections;

import static org.junit.Assert.*;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 */
public class MongoControllerTest {
    @Test
    public void testMongoController() throws Exception {
        Container container = new Container();
        container.registerResourceController("storage", new MongoController(), new SimpleConfig(Collections.<String, Object> singletonMap("db", "test")));

        UnsecureServer server = new UnsecureServer(container, InetAddress.getByName("localhost"), 8080, new NioEventLoopGroup());

        System.err.println("START SERVER");
        server.start();
        System.err.println("STARTED SERVER");

        CloseableHttpClient httpClient = HttpClientBuilder.create().build();
        HttpGet get = new HttpGet("http://localhost:8080/storage/movies");
        get.addHeader("Accept", "application/json");
        System.err.println("DO GET");
        try {
            CloseableHttpResponse result = httpClient.execute(get);
            System.err.println("=============>>>");
            System.err.println(result);
            result.getEntity().writeTo(System.err);
            System.err.println("\n<<<=============");
            assertEquals(200, result.getStatusLine().getStatusCode());
        } finally {

            System.err.println("closing");
            httpClient.close();
            System.err.println("closed");

            server.stop();
        }
    }
}
