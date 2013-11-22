/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.stomp.server;

import io.liveoak.stomp.StompMessage;
import io.netty.channel.Channel;

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
        this.channel.writeAndFlush(message);
    }

    private String connectionId;
    private Channel channel;

}
