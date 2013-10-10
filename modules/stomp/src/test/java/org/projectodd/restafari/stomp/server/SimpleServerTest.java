package org.projectodd.restafari.stomp.server;

import org.junit.Test;
import static org.junit.Assert.*;

import org.projectodd.restafari.stomp.StompMessage;
import org.projectodd.restafari.stomp.client.StompClient;

import java.util.concurrent.CountDownLatch;

/**
 * @author Bob McWhirter
 */
public class SimpleServerTest {

    @Test
    public void testSync() throws Exception {

        MockServerContext serverContext = new MockServerContext();

        SimpleStompServer server = new SimpleStompServer("localhost", 8675, serverContext);
        server.start();

        StompClient client = new StompClient();
        client.connectSync("localhost", 8675);
        client.send( "/people/bob", "howdy!" );
        client.disconnectSync();
        server.stop();

        assertEquals(1, serverContext.getSentMessages().size());

        StompMessage msg = serverContext.getSentMessages().get(0);

        assertEquals( "/people/bob", msg.getDestination() );
        assertEquals( "howdy!", msg.getContentAsString() );
    }

    @Test
    public void testAsync() throws Exception {
        MockServerContext serverContext = new MockServerContext();

        SimpleStompServer server = new SimpleStompServer("localhost", 8675, serverContext);
        server.start();

        CountDownLatch latch = new CountDownLatch(1);
        StompClient client = new StompClient();
        client.connect("localhost", 8675, (c) -> {
            c.send( "/people/bob", "dude..." );
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

        assertEquals( "/people/bob", msg.getDestination() );
        assertEquals( "dude...", msg.getContentAsString() );
    }
}
