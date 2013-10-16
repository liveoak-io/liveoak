package org.projectodd.restafari.container;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.buffer.Unpooled;
import io.netty.channel.nio.NioEventLoopGroup;

import java.net.InetAddress;
import java.nio.charset.Charset;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.*;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicHeader;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.projectodd.restafari.spi.state.CollectionResourceState;
import org.projectodd.restafari.spi.state.ObjectResourceState;
import org.projectodd.restafari.spi.state.ResourceState;

import static org.fest.assertions.Assertions.*;

public class BasicServerTest {

    static class ObjectHolder {
        public Object object;
    }

    private DefaultContainer container;
    private UnsecureServer server;

    private CloseableHttpClient httpClient;

    @Before
    public void setUpServer() throws Exception {
        this.container = new DefaultContainer();
        InMemoryDBResource resource = new InMemoryDBResource("memory");
        this.container.registerResource(resource, new SimpleConfig());

        this.server = new UnsecureServer(this.container, InetAddress.getByName("localhost"), 8080);
        this.server.start();
    }

    @Before
    public void setUpClient() throws Exception {
        this.httpClient = HttpClientBuilder.create().build();
    }

    @After
    public void tearDownClient() throws Exception {
        this.httpClient.close();
    }

    @After
    public void tearDownServer() throws Exception {
        this.server.stop();
        System.err.flush();
    }

    private ResourceState decode(HttpResponse response) throws Exception {
        ByteBuf buffer = Unpooled.buffer();
        ByteBufOutputStream out = new ByteBufOutputStream(buffer);
        response.getEntity().writeTo(out);
        out.flush();
        out.close();
        System.err.println("===================");
        System.err.println(buffer.toString(Charset.defaultCharset()));
        System.err.println("===================");
        return this.container.getCodecManager().decode("application/json", buffer);
    }

    @Test
    public void testServer() throws Exception {

        Header header = new BasicHeader("Accept", "application/json");

        HttpGet getRequest = null;
        HttpPost postRequest = null;
        HttpPut putRequest = null;
        CloseableHttpResponse response = null;

        System.err.println("TEST #1");
        // Root resource should exist.
        getRequest = new HttpGet("http://localhost:8080/memory");
        getRequest.addHeader(header);

        response = this.httpClient.execute(getRequest);

        //  {
        //    "id" : "memory",
        //    "self" : {
        //      "href" : "/memory"
        //    },
        //   "members" : [ ]
        // }

        assertThat(response).isNotNull();
        assertThat(response.getStatusLine().getStatusCode()).isEqualTo(200);

        ResourceState state = decode(response);
        assertThat(state).isNotNull();
        assertThat(state).isInstanceOf(CollectionResourceState.class);
        assertThat(((CollectionResourceState) state).members().count()).isEqualTo(0);

        response.close();

        System.err.println("TEST #2");
        // people collection should not exist.

        getRequest = new HttpGet("http://localhost:8080/memory/people");

        response = this.httpClient.execute(getRequest);
        assertThat(response).isNotNull();
        assertThat(response.getStatusLine().getStatusCode()).isEqualTo(404);

        response.close();

        System.err.println("TEST #3");
        // create people collection with direct PUT

        putRequest = new HttpPut("http://localhost:8080/memory/people");
        putRequest.setEntity(new StringEntity("{ \"members\": [] }"));
        response = this.httpClient.execute(putRequest);
        assertThat(response).isNotNull();
        assertThat(response.getStatusLine().getStatusCode()).isEqualTo(201);

        response.close();

        System.err.println("TEST #4");
        // people collection should exist now.

        getRequest = new HttpGet("http://localhost:8080/memory/people");

        response = this.httpClient.execute(getRequest);
        assertThat(response).isNotNull();
        assertThat(response.getStatusLine().getStatusCode()).isEqualTo(200);

        response.close();

        System.err.println("TEST #5");
        // people collection should be enumerable from the root


        getRequest = new HttpGet("http://localhost:8080/memory");
        getRequest.addHeader(header);

        response = this.httpClient.execute(getRequest);

        //        {
        //          "id" : "memory",
        //          "self" : {
        //            "href" : "/memory"
        //          },
        //          "members" : [ {
        //            "id" : "people",
        //            "self" : {
        //                "href" : "/memory/people"
        //            },
        //            "members" : [ ]
        //          } ]
        //        }

        assertThat(response).isNotNull();
        assertThat(response.getStatusLine().getStatusCode()).isEqualTo(200);

        state = decode(response);
        assertThat(state).isNotNull();
        assertThat(state).isInstanceOf(CollectionResourceState.class);
        assertThat(((CollectionResourceState) state).members().count()).isEqualTo(1);

        ObjectResourceState memoryCollection = (ObjectResourceState) ((CollectionResourceState) state).members().findFirst().get();
        assertThat(memoryCollection.getProperty("id")).isEqualTo("people");

        ObjectResourceState selfObj = (ObjectResourceState) memoryCollection.getProperty("_self");
        assertThat(selfObj.getProperty("href")).isEqualTo("/memory/people");

        response.close();
    }

    /*
    @Test
    public void testServer() throws Exception {

        InMemoryDBResource resource = new InMemoryDBResource( "memory" );

        //resource.create(new SimpleObjectResourceState().setId("people").setString("type", "collection"));
        //resource.create(new SimpleObjectResourceState().setId("dogs").setString("type", "collection"));

        DefaultContainer container = new DefaultContainer();
        container.registerResource(resource, new SimpleConfig());

        UnsecureServer server = new UnsecureServer(container, InetAddress.getByName("localhost"), 8080, new NioEventLoopGroup());
        server.start();

        ObjectHolder subscribedObject = new ObjectHolder();

        StompClient stompClient = new StompClient();
        stompClient.connectSync("localhost", 8080);
        stompClient.subscribe("/memory/people", (msg) -> {
            subscribedObject.object = msg;
        });

        CloseableHttpClient httpClient = HttpClientBuilder.create().build();
        Header header = new BasicHeader("Accept", "application/json");
        try {
            // Ensure no people resourceStates exist
            HttpGet get = new HttpGet("http://localhost:8080/memory/people");
            get.addHeader(header);
            Collection<ResourceState> resourceStates = assertResults(httpClient.execute(get), 200);
            assertTrue(resourceStates.isEmpty());

            // Create a people resourceState
            HttpPost post = new HttpPost("http://localhost:8080/memory/people");
            post.addHeader(header);
            ResourceState createResourceState = assertResult(httpClient.execute(post), 201);
            assertNotNull(createResourceState.id());

            // Ensure newly created resourceState actually exists
            get = new HttpGet("http://localhost:8080/memory/people/" + createResourceState.id());
            get.addHeader(header);
            ResourceState resourceState = assertResult(httpClient.execute(get), 200);
            assertNotNull(resourceState);
            assertEquals(createResourceState.id(), resourceState.id());

            //TODO: Add update

            // Retrieve all people resourceStates, ensuring only 1 person exists
            get = new HttpGet("http://localhost:8080/memory/people");
            get.addHeader(header);
            resourceStates = assertResults(httpClient.execute(get), 200);
            assertEquals(1, resourceStates.size());
            assertEquals(resourceState.id(), resourceStates.iterator().next().id());

            // And finally delete the newly create resourceState
            HttpDelete delete = new HttpDelete("http://localhost:8080/memory/people/" + createResourceState.id());
            delete.addHeader(header);
            resourceState = assertResult(httpClient.execute(delete), 200);
            // let's writeState a copy of the deleted resourceState
            assertNotNull(resourceState);

            get = new HttpGet("http://localhost:8080/memory/people/" + createResourceState.id());
            get.addHeader(header);
            assertResult(httpClient.execute(get), 404);

        } finally {
            System.err.println("closing");
            httpClient.close();
            System.err.println("closed");
            server.stop();
        }

        assertNotNull( subscribedObject.object );
    }

    private static ResourceState assertResult(HttpResponse response, int status) throws Exception {
        ByteBuf buffer = Unpooled.buffer();
        ByteBufOutputStream out = new ByteBufOutputStream(buffer);
        System.err.println("=============>>>");
        System.err.println(response);
        response.getEntity().writeTo(out);
        System.err.println(buffer.toString(Charset.forName("UTF-8")));
        System.err.println("\n<<<=============");
        assertEquals(status, response.getStatusLine().getStatusCode());
        return (ResourceState) new JSONEncoder().decode(buffer);
    }

    @SuppressWarnings("unchecked")
    private static Collection<ResourceState> assertResults(HttpResponse response, int status) throws Exception {
        ByteBuf buffer = Unpooled.buffer();
        ByteBufOutputStream out = new ByteBufOutputStream(buffer);
        System.err.println("=============>>>");
        System.err.println(response);
        response.getEntity().writeTo(out);
        System.err.println(buffer.toString(Charset.forName("UTF-8")));
        System.err.println("\n<<<=============");
        assertEquals(status, response.getStatusLine().getStatusCode());
        return (Collection<ResourceState>) new JSONEncoder().decode(buffer);
    }
    */
}
