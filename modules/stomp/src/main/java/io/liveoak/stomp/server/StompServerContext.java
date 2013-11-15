/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.stomp.server;

import io.liveoak.stomp.Headers;
import io.liveoak.stomp.StompMessage;

/**
 * @author Bob McWhirter
 */
public interface StompServerContext {

    void handleConnect(StompConnection connection);
    void handleDisconnect(StompConnection connection);
    void handleSubscribe(StompConnection connection, String destination, String subscriptionId, Headers header);
    void handleUnsubscribe(StompConnection connection, String subscriptionId);
    void handleSend(StompConnection connection, StompMessage message);

}
