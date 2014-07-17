/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.stomp.server;

import java.util.UUID;

import io.liveoak.stomp.StompMessage;
import io.netty.channel.Channel;

/**
 * @author Bob McWhirter
 */
public class StompConnection {

    public StompConnection(Channel channel, String login, String passcode) {
        this.channel = channel;
        this.login = login;
        this.passcode = passcode;
        this.connectionId = UUID.randomUUID().toString();
    }

    public String getConnectionId() {
        return this.connectionId;
    }

    public String getLogin() {
        return login;
    }

    public String getPasscode() {
        return passcode;
    }

    public void send(StompMessage message) {
        this.channel.writeAndFlush(message);
    }

    private String connectionId;
    private final Channel channel;
    private final String login;
    private final String passcode;

}
