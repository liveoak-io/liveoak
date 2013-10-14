package org.projectodd.restafari.container;

import io.netty.channel.nio.NioEventLoopGroup;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicHeader;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.net.InetAddress;

import static org.junit.Assert.assertEquals;

public class AuthTest {

    private static String SAMPLE_TOKEN = "eyJhbGciOiJSUzI1NiJ9.eyJpc3N1ZWRGb3IiOiJ0ZXN0LWFwcCIsImp0aSI6IjMtMTM4MTU5MTE3NTIyNiIsImV4cCI6MTM4MTU5MTQ3NSwiaWF0IjoxMzgxNTkxMTc1LCJhdWQiOiJ0ZXN0IiwicHJuIjoiYSIsInJlYWxtX2FjY2VzcyI6eyJyb2xlcyI6WyJ1c2VyIl19fQ.c1fuWB7yz0aOqAEe5dVFEt99DXfzt7LzL1plmwC5NmNcMNoyfLWUT3HATPH1Ee-3vO05bLXoBHcIhegKNpthO6qC2az-xNoKK2rUauJVa69Xiy0dsnVqWMxAwgQMUwuES5FeH7F5ht74ndqMpTimxaXCNKiX--srpyQM1xYx91k";

    private CloseableHttpClient httpClient;
    private UnsecureServer server;

    @Before
    public void before() throws Exception {
        InMemoryObjectResourceController controller = new InMemoryObjectResourceController();
        controller.addCollection("people");
        controller.addCollection("dogs");

        Container container = new Container();
        container.registerResourceController("test", controller, new SimpleConfig());

        server = new UnsecureServer(container, InetAddress.getByName("localhost"), 8080, new NioEventLoopGroup());
        server.start();

        httpClient = HttpClientBuilder.create().build();
    }

    @After
    public void after() throws Exception {
        try {
            httpClient.close();
        } finally {
            server.stop();
        }
    }

    @Test
    public void testMissingAuthorizationHeader() throws Exception {
        HttpGet get = new HttpGet("http://localhost:8080/memory/people");
        get.addHeader(new BasicHeader("Accept", "application/json"));

        assertEquals(HttpStatus.SC_FORBIDDEN, httpClient.execute(get).getStatusLine().getStatusCode());
    }

    @Test
    public void testInvalidAuthorizationHeader() throws Exception {
        HttpGet get = new HttpGet("http://localhost:8080/memory/people");
        get.addHeader(new BasicHeader("Accept", "application/json"));
        get.addHeader(new BasicHeader("Authorization", "Basic QWxhZGRpbjpvcGVuIHNlc2FtZQ=="));

        assertEquals(HttpStatus.SC_FORBIDDEN, httpClient.execute(get).getStatusLine().getStatusCode());
    }

    @Test
    public void testTokenNoAccess() throws Exception {
        HttpGet get = new HttpGet("http://localhost:8080/memory/people");
        get.addHeader(new BasicHeader("Accept", "application/json"));
        get.addHeader(new BasicHeader("Authorization", "bearer " + SAMPLE_TOKEN));

        assertEquals(HttpStatus.SC_FORBIDDEN, httpClient.execute(get).getStatusLine().getStatusCode());
    }

    @Test
    public void testToken() throws Exception {
        HttpGet get = new HttpGet("http://localhost:8080/test/people");
        get.addHeader(new BasicHeader("Accept", "application/json"));
        get.addHeader(new BasicHeader("Authorization", "bearer " + SAMPLE_TOKEN));

        assertEquals(HttpStatus.SC_FORBIDDEN, httpClient.execute(get).getStatusLine().getStatusCode());
    }

}
