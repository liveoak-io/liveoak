/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.stomp.server;

import java.util.concurrent.CountDownLatch;

import io.liveoak.stomp.StompMessage;
import io.liveoak.stomp.client.StompClient;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * @author Bob McWhirter
 * @author Ken Finnigan
 */
public class SimpleServerTest {

    public static class ObjectHolder {
        public Object object;
    }

    @Test
    public void testSync() throws Exception {

        MockStompServerContext serverContext = new MockStompServerContext();

        SimpleStompServer server = new SimpleStompServer("localhost", 8675, serverContext);
        server.start();

        final ObjectHolder received = new ObjectHolder();
        CountDownLatch subscriptionLatch = new CountDownLatch(1);

        StompClient client = new StompClient();
        client.connectSync("localhost", 8675);
        String subID = client.subscribe("/people/bob", (subscription) -> {
            subscription.onMessage((m) -> received.object = m);
            subscription.onReceipt(() -> subscriptionLatch.countDown());
        });

        subscriptionLatch.countDown();

        client.send("/people/bob", "howdy!");
        Thread.sleep(500);

        client.unsubscribe(subID);

        client.send("/people/bob", "seeya!");
        Thread.sleep(500);

        client.disconnectSync();
        server.stop();

        assertNotNull(received.object);
        assertTrue(received.object instanceof StompMessage);
        assertEquals("/people/bob", ((StompMessage) received.object).destination());
        assertEquals("howdy!", ((StompMessage) received.object).utf8Content());

        assertEquals(2, serverContext.getSentMessages().size());

        StompMessage msg = serverContext.getSentMessages().get(0);
        assertEquals("/people/bob", msg.destination());
        assertEquals("howdy!", msg.utf8Content());

        msg = serverContext.getSentMessages().get(1);
        assertEquals("/people/bob", msg.destination());
        assertEquals("seeya!", msg.utf8Content());
    }

    @Test
    public void testAsync() throws Exception {
        MockStompServerContext serverContext = new MockStompServerContext();

        SimpleStompServer server = new SimpleStompServer("localhost", 8675, serverContext);
        server.start();

        CountDownLatch latch = new CountDownLatch(1);
        StompClient client = new StompClient();
        client.connect("localhost", 8675, (c) -> {
            c.send("/people/bob", "dude...");
            c.disconnect(() -> {
                try {
                    server.stop();
                    latch.countDown();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });
        });

        latch.await();

        assertEquals(1, serverContext.getSentMessages().size());
        StompMessage msg = serverContext.getSentMessages().get(0);

        assertEquals("/people/bob", msg.destination());
        assertEquals("dude...", msg.utf8Content());

        server.stop();
    }
}
