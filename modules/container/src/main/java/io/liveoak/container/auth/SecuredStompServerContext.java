/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */

package io.liveoak.container.auth;

import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;

import io.liveoak.common.DefaultRequestAttributes;
import io.liveoak.common.DefaultSecurityContext;
import io.liveoak.common.codec.DefaultResourceState;
import io.liveoak.common.codec.ResourceCodecManager;
import io.liveoak.common.security.AuthzConstants;
import io.liveoak.container.subscriptions.ContainerStompServerContext;
import io.liveoak.spi.RequestAttributes;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.RequestType;
import io.liveoak.spi.ResourcePath;
import io.liveoak.spi.SecurityContext;
import io.liveoak.spi.client.Client;
import io.liveoak.spi.client.ClientResourceResponse;
import io.liveoak.spi.container.SubscriptionManager;
import io.liveoak.spi.state.ResourceState;
import io.liveoak.stomp.Headers;
import io.liveoak.stomp.StompMessage;
import io.liveoak.stomp.common.DefaultStompMessage;
import io.liveoak.stomp.server.StompConnection;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.jboss.logging.Logger;

/**
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class SecuredStompServerContext extends ContainerStompServerContext {

    private static final Logger log = Logger.getLogger(SecuredStompServerContext.class);

    private final Client client;

    public SecuredStompServerContext(ResourceCodecManager codecManager, SubscriptionManager subscriptionManager, Client client) {
        super(codecManager, subscriptionManager);
        this.client = client;
    }

    @Override
    public void handleSubscribe(StompConnection connection, String destination, String subscriptionId, Headers header) {
        String prefix = getApplicationPrefix(destination, subscriptionId);
        if (prefix == null) {
            sendError(connection, "Invalid destination: " + destination, null, subscriptionId, HttpResponseStatus.INTERNAL_SERVER_ERROR.code());
            return;
        }

        String token = null;

        // Token will remain null for anonymous requests
        if (connection.getLogin() != null) {

            // Fow now asume that "login" has always value "Bearer" and passcode is value of accessToken
            if (!"Bearer".equalsIgnoreCase(connection.getLogin())) {
                sendError(connection, "Invalid authentication type: " + connection.getLogin(), null, subscriptionId, HttpResponseStatus.UNAUTHORIZED.code());
                return;
            }
            token = connection.getPasscode();
        }
        if (log.isTraceEnabled()) {
            log.trace("Token from request: " + token);
        }

        SecuredSubscriptionFlow flow = new SecuredSubscriptionFlow(connection, destination, subscriptionId, header, token, prefix);
        flow.triggerAuth();
    }

    protected String getApplicationPrefix(String uri, String subscriptionId) {
        StringTokenizer tokens = new StringTokenizer(uri, "/");
        if (tokens.hasMoreElements()) {
            return tokens.nextToken();
        } else {
            return null;
        }
    }

    private void sendError(StompConnection connection, String message, Throwable cause, String subscriptionId, int status) {
        StompMessage errorMessage = new DefaultStompMessage(true);
        errorMessage.headers().put("status", String.valueOf(status));
        String errorMessageContent = (message != null) ? message : cause.getClass().getName() + ": " + cause.getMessage();
        errorMessage.content(errorMessageContent);
        errorMessage.headers().put(Headers.RECEIPT_ID, subscriptionId);

        log.warn("Sending stomp error to client. status: " + status + ", subscriptionId: " + subscriptionId + ", message: " + message);
        connection.send(errorMessage);
    }


    // Helper per-request object, which handles single auth+authz flow
    public class SecuredSubscriptionFlow {

        private final StompConnection connection;
        private final String destination;
        private final String subscriptionId;
        private final Headers headers;
        private final String applicationPrefix;
        private final String token;
        private SecurityContext securityContext;

        public SecuredSubscriptionFlow(StompConnection connection, String destination, String subscriptionId, Headers headers,
                                       String token, String applicationPrefix) {
            this.connection = connection;
            this.destination = destination;
            this.subscriptionId = subscriptionId;
            this.headers = headers;
            this.applicationPrefix = applicationPrefix;
            this.token = token;
        }

        protected void triggerAuth() {
            final RequestContext tokenRequestContext = new RequestContext.Builder().build();
            DefaultSecurityContext securityContext = new DefaultSecurityContext();

            // For anonymous request, just resend to authz with empty SecurityContext
            if (token == null) {
                this.securityContext = securityContext;
                triggerAuthz();
                return;
            }

            try {
                client.read(tokenRequestContext, applicationPrefix + "/auth/token-info/" + token, (ClientResourceResponse resourceResponse) -> {
                    try {
                        if (resourceResponse.responseType() == ClientResourceResponse.ResponseType.NO_SUCH_RESOURCE) {
                            this.securityContext = securityContext;
                            triggerAuthz();
                            return;
                        }

                        ResourceState state = resourceResponse.state();
                        if (state.getProperty("error") != null) {
                            sendError("Failed to authenticate token. Error: " + state.getProperty("error"), null, HttpResponseStatus.UNAUTHORIZED.code());
                            return;
                        } else {
                            securityContext.setOriginal(token);

                            securityContext.setRealm((String) state.getProperty("realm"));
                            securityContext.setSubject((String) state.getProperty("subject"));
                            securityContext.setLastVerified(((Date) state.getProperty("issued-at")).getTime());

                            Set<String> roles = new HashSet<>();
                            roles.addAll((Collection<? extends String>) state.getProperty("roles"));
                            securityContext.setRoles(roles);

                            this.securityContext = securityContext;
                            triggerAuthz();
                        }
                    } catch (Throwable t) {
                        sendError(null, t, HttpResponseStatus.INTERNAL_SERVER_ERROR.code());
                    }
                });
            } catch (Throwable t) {
                sendError(null, t, HttpResponseStatus.INTERNAL_SERVER_ERROR.code());
            }
        }

        protected void triggerAuthz() {
            try {
                // Build requestContext from stomp destination and subscriptionId and put it as attribute of the request, which will be sent to AuthzService
                RequestAttributes attribs = new DefaultRequestAttributes();
                RequestContext reqContext = createRequestContext();
                attribs.setAttribute(AuthzConstants.ATTR_REQUEST_CONTEXT, reqContext);
                attribs.setAttribute(AuthzConstants.ATTR_REQUEST_RESOURCE_STATE, new DefaultResourceState());
                attribs.setAttribute(AuthzConstants.ATTR_RESPONSE_RESOURCE_STATE, new DefaultResourceState());
                RequestContext authzRequest = new RequestContext.Builder().requestAttributes(attribs).build();

                client.read(authzRequest, applicationPrefix + "/authz/authzCheck", (ClientResourceResponse resourceResponse) -> {

                    // Authorize automatically if Authz service is not available
                    if (resourceResponse.responseType() == ClientResourceResponse.ResponseType.NO_SUCH_RESOURCE) {
                        SecuredStompServerContext.super.handleSubscribeSecured(connection, destination, subscriptionId, headers, securityContext);
                        return;
                    }

                    try {
                        ResourceState state = resourceResponse.state();
                        boolean authorized = (Boolean) state.getProperty(AuthzConstants.ATTR_AUTHZ_RESULT);

                        if (authorized) {
                            SecuredStompServerContext.super.handleSubscribeSecured(connection, destination, subscriptionId, headers, securityContext);
                        } else {
                            boolean authenticated = securityContext.isAuthenticated();
                            int errorType = authenticated ? HttpResponseStatus.FORBIDDEN.code() : HttpResponseStatus.UNAUTHORIZED.code();
                            sendError("Authorization failed", null, errorType);
                        }
                    } catch (Throwable t) {
                        sendError(null, t, HttpResponseStatus.INTERNAL_SERVER_ERROR.code());
                    }
                });
            } catch (Throwable t) {
                sendError(null, t, HttpResponseStatus.INTERNAL_SERVER_ERROR.code());
            }
        }

        private RequestContext createRequestContext() {
            return new RequestContext.Builder()
                    .requestType(RequestType.READ)
                    .resourcePath(new ResourcePath(destination))
                    .securityContext(securityContext).build();
        }

        private void sendError(String message, Throwable cause, int status) {
            log.error(message, cause);
            SecuredStompServerContext.this.sendError(connection, message, cause, subscriptionId, status);
        }
    }

}
