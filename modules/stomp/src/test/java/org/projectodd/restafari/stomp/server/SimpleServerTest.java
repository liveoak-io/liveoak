package org.projectodd.restafari.stomp.server;

import org.junit.Test;

import static org.junit.Assert.*;

import org.projectodd.restafari.stomp.StompMessage;
import org.projectodd.restafari.stomp.client.StompClient;

import java.lang.ref.Reference;
import java.sql.Ref;
import java.util.concurrent.CountDownLatch;

/**
 * @author Bob McWhirter
 */
public class SimpleServerTest {

    public static class ObjectHolder {
        public Object object;
    }

    @Test
    public void testSync() throws Exception {

        MockServerContext serverContext = new MockServerContext();

        SimpleStompServer server = new SimpleStompServer("localhost", 8675, serverContext);
        server.start();

        final ObjectHolder received = new ObjectHolder();

        StompClient client = new StompClient();
        client.connectSync("localhost", 8675);
        client.subscribe("/people/bob", (m) -> {
            received.object = m;
        });
        client.send("/people/bob", "howdy!");
        Thread.sleep(1000);
        client.disconnectSync();
        server.stop();

        assertNotNull(received.object);
        assertTrue(received.object instanceof StompMessage);
        assertEquals("/people/bob", ((StompMessage) received.object).getDestination());
        assertEquals("howdy!", ((StompMessage) received.object).getContentAsString());

        assertEquals(1, serverContext.getSentMessages().size());

        StompMessage msg = serverContext.getSentMessages().get(0);

        assertEquals("/people/bob", msg.getDestination());
        assertEquals("howdy!", msg.getContentAsString());
    }

    @Test
    public void testAsync() throws Exception {
        MockServerContext serverContext = new MockServerContext();

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

        StompMessage msg = serverContext.getSentMessages().get(0);

        assertEquals("/people/bob", msg.getDestination());
        assertEquals("dude...", msg.getContentAsString());
    }
}
