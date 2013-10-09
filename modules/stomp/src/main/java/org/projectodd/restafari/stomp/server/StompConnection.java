package org.projectodd.restafari.stomp.server;

import org.projectodd.restafari.stomp.StompMessage;

import java.util.UUID;

/**
 * @author Bob McWhirter
 */
public class StompConnection {

    public StompConnection() {
        this.connectionId = UUID.randomUUID().toString();
    }

    public String getConnectionId() {
        return this.connectionId;
    }

    void send(StompMessage message) {

    }

    private String connectionId;

}
