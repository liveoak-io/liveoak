package org.projectodd.restafari.container;

import org.projectodd.restafari.container.codec.ResourceCodec;
import org.projectodd.restafari.container.subscriptions.StompSubscription;
import org.projectodd.restafari.stomp.Headers;
import org.projectodd.restafari.stomp.StompMessage;
import org.projectodd.restafari.stomp.server.StompServerContext;
import org.projectodd.restafari.stomp.server.StompConnection;

/**
 * @author Bob McWhirter
 */
public class ContainerStompServerContext implements StompServerContext {

    public ContainerStompServerContext(DefaultContainer container) {
        this.container = container;
    }

    @Override
    public void handleConnect(StompConnection connection) {
    }

    @Override
    public void handleDisconnect(StompConnection connection) {
    }

    @Override
    public void handleSubscribe(StompConnection connection, String destination, String subscriptionId, Headers headers) {
        String contentType = headers.get( "accept" );
        if ( contentType == null ) {
            contentType = "application/json";
        }
        ResourceCodec codec = this.container.getCodecManager().getResourceCodec(contentType);
        StompSubscription subscription = new StompSubscription(connection, destination, subscriptionId, contentType, codec);
        this.container.getSubscriptionManager().addSubscription( subscription );
    }

    @Override
    public void handleUnsubscribe(StompConnection connection, String subscriptionId) {
    }

    @Override
    public void handleSend(StompConnection connection, StompMessage message) {
    }

    private DefaultContainer container;
}
