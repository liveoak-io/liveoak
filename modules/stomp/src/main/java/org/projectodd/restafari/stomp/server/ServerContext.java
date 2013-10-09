package org.projectodd.restafari.stomp.server;

import org.projectodd.restafari.stomp.Headers;
import org.projectodd.restafari.stomp.StompMessage;

/**
 * @author Bob McWhirter
 */
public interface ServerContext {

    void handleConnect(StompConnection connection);
    void handleDisconnect(StompConnection connection);
    void handleSubscribe(StompConnection connection, String destination, String subscriptionId, Headers header);
    void handleUnsubscribe(StompConnection connection, String subscriptionId);
    void handleSend(StompConnection connection, StompMessage message);

}
