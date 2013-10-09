package org.projectodd.restafari.stomp.server;

import org.junit.Test;
import org.projectodd.restafari.stomp.client.StompClient;
import org.projectodd.restafari.stomp.common.DefaultStompMessage;

/**
 * @author Bob McWhirter
 */
public class SimpleServerTest {

    @Test
    public void testSomething() throws Exception {

        MockServerContext serverContext = new MockServerContext();

        SimpleServer server = new SimpleServer("localhost", 8675, serverContext );
        server.start();

        StompClient client = new StompClient();
        client.connectSync( "localhost", 8675 );
        client.disconnectSync();
        server.stop();


    }
}
