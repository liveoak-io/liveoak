package org.projectodd.restafari.stomp.server;

import io.netty.channel.Channel;
import org.projectodd.restafari.stomp.StompMessage;

import java.util.UUID;

/**
 * @author Bob McWhirter
 */
public class StompConnection {

    public StompConnection(Channel channel) {
        this.channel = channel;
        this.connectionId = UUID.randomUUID().toString();
    }

    public String getConnectionId() {
        return this.connectionId;
    }

    public void send(StompMessage message) {
        this.channel.write( message );
    }

    private String connectionId;
    private Channel channel;

}
