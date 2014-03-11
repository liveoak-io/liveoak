/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */

package io.liveoak.stomp.server.security;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import io.liveoak.stomp.Headers;
import io.liveoak.stomp.Stomp;
import io.liveoak.stomp.StompMessage;
import io.liveoak.stomp.client.StompClient;
import io.liveoak.stomp.server.SimpleStompServer;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class SecuredServerTest {

    @Test
    public void testAuth() throws Exception {

        MockAuthServerContext serverContext = new MockAuthServerContext();
        SimpleStompServer server = new SimpleStompServer("localhost", 8675, serverContext);
        server.start();

        // authenticates success for john/john
        CountDownLatch subscriptionLatch = new CountDownLatch(1);
        MockErrorConsumer errorConsumer = new MockErrorConsumer().latch(subscriptionLatch);
        StompClient client = new StompClient(errorConsumer);
        client.connectSync("localhost", 8675, "john", "john");
        client.subscribe("/john/allowed", (subscription) -> {
            subscription.onReceipt( ()->{
                subscriptionLatch.countDown();
            });
        });
        subscriptionLatch.await(10000, TimeUnit.MILLISECONDS);
        Assert.assertFalse(errorConsumer.error);
        Assert.assertEquals("john", serverContext.getLastConnectedLogin());
        Assert.assertEquals("john", serverContext.getLastSubscribedLogin());

        // authentication failed for john/invalid
        CountDownLatch subscriptionLatch2 = new CountDownLatch(1);
        errorConsumer = new MockErrorConsumer().latch(subscriptionLatch2);
        client = new StompClient(errorConsumer);
        client.connect("localhost", 8675, "john", "invalid", (c) -> {
            Assert.fail("Shouldn't be connected");
        });
        subscriptionLatch2.await(10000, TimeUnit.MILLISECONDS);
        Assert.assertTrue(errorConsumer.error);
        Assert.assertEquals(HttpResponseStatus.UNAUTHORIZED.code(), errorConsumer.status);
        Assert.assertNull(serverContext.getLastConnectedLogin());

        // authorization failed at subscription time
        CountDownLatch subscriptionLatch3 = new CountDownLatch(1);
        errorConsumer = new MockErrorConsumer().latch(subscriptionLatch3);
        client = new StompClient(errorConsumer);
        client.connectSync("localhost", 8675, "john", "john");
        client.subscribe("/bob/notAllowed", (subscription) -> {
            subscription.onReceipt(() -> {
                Assert.fail("Shouldn't be subscribed");
            });
        });
        subscriptionLatch3.await();
        Assert.assertTrue(errorConsumer.error);
        Assert.assertEquals(HttpResponseStatus.FORBIDDEN.code(), errorConsumer.status);
        Assert.assertNotNull(errorConsumer.message.headers().get(Headers.RECEIPT_ID));
        Assert.assertEquals("john", serverContext.getLastConnectedLogin());
        Assert.assertNull(serverContext.getLastSubscribedLogin());

        // authentication failed for john/invalid
        CountDownLatch subscriptionLatch4 = new CountDownLatch(1);
        errorConsumer = new MockErrorConsumer().latch(subscriptionLatch4);
        client = new StompClient(errorConsumer);
        client.connect("localhost", 8675, (c) -> {
            Assert.fail("Shouldn't be connected");
        });
        subscriptionLatch4.await(10000, TimeUnit.MILLISECONDS);
        Assert.assertTrue(errorConsumer.error);
        Assert.assertNull(serverContext.getLastConnectedLogin());
    }

    private class MockErrorConsumer implements Consumer<StompMessage> {

        private CountDownLatch latch;
        private StompMessage message;
        private boolean error = false;
        private int status;

        private MockErrorConsumer latch(CountDownLatch latch) {
            this.latch = latch;
            return this;
        }

        @Override
        public void accept(StompMessage stompMessage) {
            this.message = stompMessage;
            this.error = true;
            this.status = Integer.parseInt(stompMessage.headers().get("status"));
            latch.countDown();
        }
    }
}
