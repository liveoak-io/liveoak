/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.container;

import java.nio.charset.Charset;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import io.liveoak.container.tenancy.InternalApplication;
import io.liveoak.spi.LiveOak;
import io.liveoak.spi.MediaType;
import io.liveoak.spi.state.ResourceState;
import io.liveoak.stomp.StompMessage;
import io.liveoak.stomp.client.StompClient;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.buffer.Unpooled;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicHeader;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;

public class BasicServerTest extends AbstractContainerTest {

    protected CloseableHttpClient httpClient;

    @Before
    public void setUpServer() throws Exception {
        system = LiveOakFactory.create();
        system.extensionInstaller().load("memory", new InMemoryDBExtension());

        awaitStability();
        InternalApplication application = system.applicationRegistry().createApplication("testApp", "Test Application");

        application.extend("memory");
        awaitStability();
    }

    @Before
    public void setUpClient() throws Exception {
        this.httpClient = HttpClientBuilder.create().build();
    }

    @After
    public void tearDownClient() throws Exception {
        this.httpClient.close();
    }

    @AfterClass
    public static void tearDownServer() throws Exception {
        system.stop();
        System.err.flush();
    }

    protected ResourceState decode(HttpResponse response) throws Exception {
        ByteBuf buffer = Unpooled.buffer();
        ByteBufOutputStream out = new ByteBufOutputStream(buffer);
        response.getEntity().writeTo(out);
        out.flush();
        out.close();
        System.err.println("===================");
        System.err.println(buffer.toString(Charset.defaultCharset()));
        System.err.println("===================");
        return system.codecManager().decode(MediaType.JSON, buffer);
    }

    protected ResourceState decode(ByteBuf buffer) throws Exception {
        System.err.println("===================");
        System.err.println(buffer.toString(Charset.defaultCharset()));
        System.err.println("===================");
        return system.codecManager().decode(MediaType.JSON, buffer);
    }

    @Test
    public void testServer() throws Exception {

        CompletableFuture<StompMessage> peopleCreationNotification = new CompletableFuture<>();
        CompletableFuture<StompMessage> bobCreationNotification = new CompletableFuture<>();

        StompClient stompClient = new StompClient();

        CountDownLatch subscriptionLatch = new CountDownLatch(1);

        stompClient.connect("localhost", 8080, (client) -> {
            stompClient.subscribe("/testApp/memory/people/*", (subscription) -> {
                subscription.onMessage((msg) -> {
                    System.err.println("******* MESSAGE: " + msg);
                    if (msg.headers().get("location").equals("/testApp/memory/people")) {
                        peopleCreationNotification.complete(msg);
                    } else {
                        bobCreationNotification.complete(msg);
                    }
                });
                subscription.onReceipt(() -> {
                    subscriptionLatch.countDown();
                });
            });
        });

        subscriptionLatch.await();

        Header header = new BasicHeader("Accept", "application/json");

        HttpGet getRequest = null;
        HttpPost postRequest = null;
        HttpPut putRequest = null;
        CloseableHttpResponse response = null;

        System.err.println("TEST #1");
        // Root object should exist.
        getRequest = new HttpGet("http://localhost:8080/testApp/memory");
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
        assertThat(state.members().size()).isEqualTo(0);

        response.close();

        System.err.println("TEST #2");
        // people collection should not exist.

        getRequest = new HttpGet("http://localhost:8080/testApp/memory/people");

        response = this.httpClient.execute(getRequest);
        assertThat(response).isNotNull();
        assertThat(response.getStatusLine().getStatusCode()).isEqualTo(404);

        response.close();

        System.err.println("TEST #3");
        // create people collection with direct PUT

        putRequest = new HttpPut("http://localhost:8080/testApp/memory/people");
        putRequest.setEntity(new StringEntity("{ \"type\": \"collection\" }"));
        putRequest.setHeader("Content-Type", "application/json");
        response = this.httpClient.execute(putRequest);
        System.err.println("response: " + response);
        assertThat(response).isNotNull();
        assertThat(response.getStatusLine().getStatusCode()).isEqualTo(201);

        response.close();

        System.err.println("TEST #4");
        // people collection should exist now.

        getRequest = new HttpGet("http://localhost:8080/testApp/memory/people");

        response = this.httpClient.execute(getRequest);
        assertThat(response).isNotNull();
        assertThat(response.getStatusLine().getStatusCode()).isEqualTo(200);

        response.close();

        assertThat(peopleCreationNotification.getNow(null)).isNull();

        System.err.println("TEST #5");
        // people collection should be enumerable from the root


        getRequest = new HttpGet("http://localhost:8080/testApp/memory?expand=members");
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
        assertThat(state.members().size()).isEqualTo(1);

        ResourceState memoryCollection = state.members().get(0);
        assertThat(memoryCollection.id()).isEqualTo("people");

        ResourceState selfObj = (ResourceState) memoryCollection.getProperty(LiveOak.SELF);
        assertThat(selfObj.getProperty(LiveOak.HREF)).isEqualTo("/testApp/memory/people");

        System.err.println("TEST #6");
        // Post a person

        postRequest = new HttpPost("http://localhost:8080/testApp/memory/people");
        postRequest.setEntity(new StringEntity("{ \"name\": \"bob\" }"));
        postRequest.setHeader("Content-Type", "application/json");

        response = httpClient.execute(postRequest);
        assertThat(response).isNotNull();
        assertThat(response.getStatusLine().getStatusCode()).isEqualTo(201);

        state = decode(response);
        assertThat(state).isNotNull();
        assertThat(state).isInstanceOf(ResourceState.class);

        assertThat(state.id()).isNotNull();
        assertThat(state.getProperty("name")).isEqualTo("bob");


        // check STOMP
        System.err.println("TEST #STOMP");
        StompMessage obj = bobCreationNotification.get(30000, TimeUnit.SECONDS);
        assertThat(obj).isNotNull();

        ResourceState bobObjState = decode(obj.content());
        assertThat(bobObjState.getProperty("name")).isEqualTo("bob");

        assertThat(state.getProperty(LiveOak.ID)).isEqualTo(bobObjState.getProperty(LiveOak.ID));
        response.close();
    }


}
