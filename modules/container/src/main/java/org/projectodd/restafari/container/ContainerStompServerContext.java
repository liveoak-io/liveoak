package org.projectodd.restafari.container;

import org.projectodd.restafari.container.Container;
import org.projectodd.restafari.stomp.Headers;
import org.projectodd.restafari.stomp.StompMessage;
import org.projectodd.restafari.stomp.server.StompServerContext;
import org.projectodd.restafari.stomp.server.StompConnection;

/**
 * @author Bob McWhirter
 */
public class ContainerStompServerContext implements StompServerContext {

    public ContainerStompServerContext(Container container) {
        this.container = container;
    }

    @Override
    public void handleConnect(StompConnection connection) {
    }

    @Override
    public void handleDisconnect(StompConnection connection) {
    }

    @Override
    public void handleSubscribe(StompConnection connection, String destination, String subscriptionId, Headers header) {
    }

    @Override
    public void handleUnsubscribe(StompConnection connection, String subscriptionId) {
    }

    @Override
    public void handleSend(StompConnection connection, StompMessage message) {
    }

    private Container container;
}
