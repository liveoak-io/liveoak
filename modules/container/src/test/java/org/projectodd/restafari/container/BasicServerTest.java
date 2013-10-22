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
import org.projectodd.restafari.stomp.StompMessage;
import org.projectodd.restafari.stomp.client.StompClient;

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

    private ResourceState decode(ByteBuf buffer) throws Exception {
        System.err.println("===================");
        System.err.println(buffer.toString(Charset.defaultCharset()));
        System.err.println("===================");
        return this.container.getCodecManager().decode("application/json", buffer);
    }

    @Test
    public void testServer() throws Exception {

        ObjectHolder peopleCreationNotification = new ObjectHolder();
        ObjectHolder bobCreationNotification = new ObjectHolder();

        StompClient stompClient = new StompClient();
        stompClient.connect("localhost", 8080, (client) -> {
            // Subscribe only to the contents of /memory/people, and not the
            // memory/people itself plus contents.
            client.subscribe( "/memory/people/*", (msg)->{
                if ( msg.headers().get( "location" ).equals( "/memory/people" ) ) {
                    peopleCreationNotification.object = msg;
                } else {
                    bobCreationNotification.object = msg;
                }
            });
        });

        Header header = new BasicHeader("Accept", "application/json");

        HttpGet getRequest = null;
        HttpPost postRequest = null;
        HttpPut putRequest = null;
        CloseableHttpResponse response = null;

        System.err.println("TEST #1");
        // Root object should exist.
        getRequest = new HttpGet("http://localhost:8080/memory");
        getRequest.addHeader(header);

        response = this.httpClient.execute(getRequest);

        //  {
        //    "id" : "memory",
        //    "self" : {
        //      "href" : "/memory"
        //    },
        //   "content" : [ ]
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
        putRequest.setEntity(new StringEntity("{ \"content\": [] }"));
        response = this.httpClient.execute(putRequest);
        System.err.println( "response: " + response );
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

        assertThat( peopleCreationNotification.object ).isNull();

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
        //          "content" : [ {
        //            "id" : "people",
        //            "self" : {
        //                "href" : "/memory/people"
        //            },
        //            "content" : [ ]
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

        // Post a person

        postRequest = new HttpPost( "http://localhost:8080/memory/people");
        postRequest.setEntity( new StringEntity("{ \"name\": \"bob\" }" ) );

        response = httpClient.execute( postRequest );
        assertThat(response).isNotNull();
        assertThat(response.getStatusLine().getStatusCode()).isEqualTo(201);

        state = decode(response);
        assertThat(state).isNotNull();
        assertThat(state).isInstanceOf(ObjectResourceState.class);

        assertThat( ((ObjectResourceState)state).getProperty( "id" ) ).isNotNull();
        assertThat(((ObjectResourceState) state).getProperty("name")).isEqualTo( "bob" );

        // check STOMP

        Thread.sleep( 500 );

        assertThat( bobCreationNotification.object ).isNotNull();

        ObjectResourceState bobObjState = (ObjectResourceState) decode( ((StompMessage) bobCreationNotification.object ).content() );
        assertThat( bobObjState.getProperty( "name" ) ).isEqualTo( "bob" );

        assertThat( ((ObjectResourceState)state).getProperty( "id" ) ).isEqualTo( bobObjState.getProperty( "id" ) );

        response.close();
    }


}
