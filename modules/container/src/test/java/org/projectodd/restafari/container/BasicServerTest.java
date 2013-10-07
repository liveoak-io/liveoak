package org.projectodd.restafari.container;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.buffer.Unpooled;
import io.netty.channel.nio.NioEventLoopGroup;

import java.net.InetAddress;
import java.util.Collection;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicHeader;
import org.junit.Test;
import org.projectodd.restafari.container.codec.json.JSONCodec;
import org.projectodd.restafari.spi.Resource;

import static org.junit.Assert.*;

public class BasicServerTest {

    @Test
    public void testServer() throws Exception {

        InMemoryObjectResourceController controller = new InMemoryObjectResourceController();
        controller.addCollection( "people" );
        controller.addCollection( "dogs" );

        Container container = new Container();
        container.registerResourceController( "memory", controller, new SimpleConfig() );

        UnsecureServer server = new UnsecureServer(container, InetAddress.getByName("localhost"), 8080, new NioEventLoopGroup());

        System.err.println( "START SERVER" );
        server.start();
        System.err.println( "STARTED SERVER" );

        CloseableHttpClient httpClient = HttpClientBuilder.create().build();
        Header header = new BasicHeader("Accept", "application/json");
        try {
            // Ensure no people resources exist
            HttpGet get = new HttpGet( "http://localhost:8080/memory/people" );
            get.addHeader(header);
            Collection<Resource> resources = assertResults(httpClient.execute(get), 200);
            assertTrue(resources.isEmpty());

            // Create a people resource
            HttpPost post = new HttpPost("http://localhost:8080/memory/people");
            post.addHeader(header);
            Resource createResource = assertResult(httpClient.execute(post), 201);
            assertNotNull(createResource.getId());

            // Ensure newly created resource actually exists
            get = new HttpGet("http://localhost:8080/memory/people/" + createResource.getId());
            get.addHeader(header);
            Resource resource = assertResult(httpClient.execute(get), 200);
            assertNotNull(resource);
            assertEquals(createResource.getId(), resource.getId());

            //TODO: Add update

            // Retrieve all people resources, ensuring only 1 person exists
            get = new HttpGet( "http://localhost:8080/memory/people" );
            get.addHeader(header);
            resources = assertResults(httpClient.execute(get), 200);
            assertEquals(1, resources.size());
            assertEquals(resource.getId(), resources.iterator().next().getId());

            // And finally delete the newly create resource
            HttpDelete delete = new HttpDelete("http://localhost:8080/memory/people/" + createResource.getId());
            delete.addHeader(header);
            resource = assertResult(httpClient.execute(delete), 200);
            assertNull(resource);

        } finally {
            System.err.println("closing");
            httpClient.close();
            System.err.println("closed");
            server.stop();
        }
    }

    private static Resource assertResult(HttpResponse response, int status) throws Exception {
        ByteBuf buffer = Unpooled.buffer();
        ByteBufOutputStream out = new ByteBufOutputStream(buffer);
        System.err.println("=============>>>");
        System.err.println(response);
        response.getEntity().writeTo(out);
        System.err.println(new String(buffer.array(), "UTF-8"));
        System.err.println("\n<<<=============");
        assertEquals(status, response.getStatusLine().getStatusCode());
        return (Resource) new JSONCodec().decode(buffer);
    }

    @SuppressWarnings("unchecked")
    private static Collection<Resource> assertResults(HttpResponse response, int status) throws Exception {
        ByteBuf buffer = Unpooled.buffer();
        ByteBufOutputStream out = new ByteBufOutputStream(buffer);
        System.err.println("=============>>>");
        System.err.println(response);
        response.getEntity().writeTo(out);
        System.err.println(new String(buffer.array(), "UTF-8"));
        System.err.println("\n<<<=============");
        assertEquals(status, response.getStatusLine().getStatusCode());
        return (Collection<Resource>) new JSONCodec().decode(buffer);
    }
}
