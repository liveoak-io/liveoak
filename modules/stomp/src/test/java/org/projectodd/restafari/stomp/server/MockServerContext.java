package org.projectodd.restafari.stomp.server;

import org.projectodd.restafari.stomp.Headers;
import org.projectodd.restafari.stomp.StompMessage;

/**
 * @author Bob McWhirter
 */
public class MockServerContext implements ServerContext {

    @Override
    public void handleConnect(StompConnection connection) {
        System.err.println( "connected: " + connection );
    }

    @Override
    public void handleDisconnect(StompConnection connection) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void handleSubscribe(StompConnection connection, String destination, String subscriptionId, Headers header) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void handleUnsubscribe(StompConnection connection, String subscriptionId) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void handleSend(StompConnection connection, StompMessage message) {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
