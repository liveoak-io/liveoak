/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.container.subscriptions;

import io.liveoak.common.codec.ResourceCodecManager;
import io.liveoak.common.security.DefaultSecurityContext;
import io.liveoak.common.security.SecurityHelper;
import io.liveoak.spi.client.Client;
import io.liveoak.spi.container.SubscriptionManager;
import io.liveoak.stomp.StompMessage;
import io.liveoak.stomp.common.DefaultStompMessage;
import io.liveoak.stomp.server.StompConnection;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.jboss.logging.Logger;

/**
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 * @author Ken Finnigan
 */
public class SecuredStompServerContext extends ContainerStompServerContext {

    private static final Logger log = Logger.getLogger(SecuredStompServerContext.class);

    private final Client client;

    public SecuredStompServerContext(ResourceCodecManager codecManager, SubscriptionManager subscriptionManager, Client client) {
        super(codecManager, subscriptionManager);
        this.client = client;
    }

    @Override
    public void handleConnect(StompConnection connection, String applicationId) {
        String token = null;

        // Token will remain null for anonymous requests
        if (connection.getLogin() != null) {
            // For now assume that "login" has always value "Bearer" and passcode is value of accessToken
            if (!"Bearer".equalsIgnoreCase(connection.getLogin())) {
                sendError(connection, "Invalid authentication type: " + connection.getLogin(), null, HttpResponseStatus.UNAUTHORIZED.code());
                return;
            }
            token = connection.getPasscode();
        }

        DefaultSecurityContext securityContext = new DefaultSecurityContext();

        // For anonymous request, just set empty SecurityContext
        if (token == null) {
            connection.setSecurityContext(securityContext);
            return;
        }

        if (log.isTraceEnabled()) {
            log.trace("Token from request: " + token);
        }

        SecurityHelper.auth(client, securityContext, applicationId, token,
                // Success function
                () -> connection.setSecurityContext(securityContext),
                // No Such Resource function
                () -> connection.setSecurityContext(securityContext),
                // Not Authorized function
                error -> sendError(connection, "Failed to authenticate token. Error: " + error, null, HttpResponseStatus.UNAUTHORIZED.code()),
                // Handle throwable
                throwable -> sendError(connection, null, throwable, HttpResponseStatus.INTERNAL_SERVER_ERROR.code()));
    }

    private void sendError(StompConnection connection, String message, Throwable cause, int status) {
        StompMessage errorMessage = new DefaultStompMessage(true);
        errorMessage.headers().put("status", String.valueOf(status));
        String errorMessageContent = (message != null) ? message : cause.getClass().getName() + ": " + cause.getMessage();
        errorMessage.content(errorMessageContent);

        log.warn("Sending stomp error to client. status: " + status + ", message: " + message);
        connection.send(errorMessage);
    }

}
