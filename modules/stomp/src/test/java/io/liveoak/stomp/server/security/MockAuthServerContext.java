/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */

package io.liveoak.stomp.server.security;

import io.liveoak.stomp.Headers;
import io.liveoak.stomp.Stomp;
import io.liveoak.stomp.StompMessage;
import io.liveoak.stomp.server.StompConnection;
import io.liveoak.stomp.server.StompServerContext;
import io.liveoak.stomp.server.StompServerException;
import io.liveoak.stomp.server.StompServerSecurityException;
import io.netty.handler.codec.http.HttpResponseStatus;

/**
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class MockAuthServerContext implements StompServerContext {

    private String lastConnectedLogin;
    private String lastSubscribedLogin;

    @Override
    public void handleConnect(StompConnection connection, String applicationId) throws StompServerException {
        String login = connection.getLogin();
        String passcode = connection.getPasscode();

        // Dummy authentication pass if username and password are same
        if (login != null && login.equals(passcode)) {
            lastConnectedLogin = login;
        } else {
            lastConnectedLogin = null;
            StompServerException ex = new StompServerSecurityException("Authentication failed for " + login)
                    .withCommand(Stomp.Command.CONNECT)
                    .withStatus(HttpResponseStatus.UNAUTHORIZED.code());
            throw ex;
        }
    }

    @Override
    public void handleDisconnect(StompConnection connection) {
    }

    @Override
    public void handleSubscribe(StompConnection connection, String destination, String subscriptionId, Headers header) throws StompServerException {
        String login = connection.getLogin();
        String passcode = connection.getPasscode();
        String errorMessage = null;
        int status = 0;

        // Dummy authentication pass if username and password are same
        if (login != null && login.equals(passcode)) {

            // Dummy authorization pass if destination starts with username
            if (destination.startsWith("/" + login)) {
                lastSubscribedLogin = login;
            } else {
                lastSubscribedLogin = null;
                errorMessage = "Authorization failed. Destination: " + destination + ", login: " + login;
                status = HttpResponseStatus.FORBIDDEN.code();
            }
        } else {
            lastSubscribedLogin = null;
            errorMessage = "Authentication failed. Login: " + login;
            status = HttpResponseStatus.UNAUTHORIZED.code();
        }

        if (errorMessage != null) {
            StompServerException ex = new StompServerSecurityException(errorMessage)
                    .withReceiptId(subscriptionId)
                    .withCommand(Stomp.Command.SUBSCRIBE)
                    .withStatus(status);
            throw ex;
        }
    }

    @Override
    public void handleUnsubscribe(StompConnection connection, String subscriptionId) {
    }

    @Override
    public void handleSend(StompConnection connection, StompMessage message) {
    }

    public String getLastConnectedLogin() {
        return lastConnectedLogin;
    }

    public String getLastSubscribedLogin() {
        return lastSubscribedLogin;
    }
}
