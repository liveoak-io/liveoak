/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
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

    void handleConnect(StompConnection connection, String applicationId) throws StompServerException;

    void handleDisconnect(StompConnection connection) throws StompServerException;

    void handleSubscribe(StompConnection connection, String destination, String subscriptionId, Headers header) throws StompServerException;

    void handleUnsubscribe(StompConnection connection, String subscriptionId) throws StompServerException;

    void handleSend(StompConnection connection, StompMessage message) throws StompServerException;

}
