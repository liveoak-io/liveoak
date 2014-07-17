/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.keycloak.interceptor;

import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

import io.liveoak.common.DefaultResourceErrorResponse;
import io.liveoak.common.DefaultSecurityContext;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.ResourceErrorResponse;
import io.liveoak.spi.ResourcePath;
import io.liveoak.spi.ResourceRequest;
import io.liveoak.spi.client.Client;
import io.liveoak.spi.client.ClientResourceResponse;
import io.liveoak.spi.container.interceptor.DefaultInterceptor;
import io.liveoak.spi.container.interceptor.InboundInterceptorContext;
import io.liveoak.spi.state.ResourceState;
import io.netty.handler.codec.http.HttpHeaders;
import org.jboss.logging.Logger;

/**
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
public class AuthInterceptor extends DefaultInterceptor {

    public static final String AUTH_TYPE = "bearer";
    private static final Logger log = Logger.getLogger(AuthInterceptor.class);

    private Client client;

    public AuthInterceptor(Client client) {
        this.client = client;
    }

    @Override
    public void onInbound(InboundInterceptorContext context) throws Exception {
        ResourceRequest req = context.request();
        final RequestContext requestContext = req.requestContext();
        final DefaultSecurityContext securityContext = (DefaultSecurityContext) requestContext.securityContext();
        final String token = getBearerToken(requestContext);
        if (token != null) {
            initSecurityContext(context, req, securityContext, token);
        } else {
            context.forward();
        }
    }

    private String getPrefix(ResourcePath path) {
        String prefix = "/" + path.head().name();
        return prefix;
    }

    private void initSecurityContext(final InboundInterceptorContext context, final ResourceRequest req, final DefaultSecurityContext securityContext, String token) {
        final RequestContext tokenRequestContext = new RequestContext.Builder().build();
        String prefix = getPrefix(req.resourcePath());
        try {
            client.read(tokenRequestContext, prefix + "/auth/token-info/" + token, new Consumer<ClientResourceResponse>() {
                @Override
                public void accept(ClientResourceResponse resourceResponse) {
                    try {
                        ResourceState state = resourceResponse.state();
                        if (resourceResponse.responseType().equals(ClientResourceResponse.ResponseType.NO_SUCH_RESOURCE)) {
                            log.info("Auth not configured for " + prefix);
                            context.forward();
                        } else if (state.getProperty("error") != null) {
                            log.warn("Authentication failed. Request: " + req + ", error: " + state.getProperty("error"));
                            context.replyWith(new DefaultResourceErrorResponse(req, ResourceErrorResponse.ErrorType.NOT_AUTHORIZED));
                        } else {
                            securityContext.setOriginal(token);

                            securityContext.setRealm((String) state.getProperty("realm"));
                            securityContext.setSubject((String) state.getProperty("subject"));
                            securityContext.setLastVerified(((Date) state.getProperty("issued-at")).getTime());

                            Set<String> roles = new HashSet<>();
                            roles.addAll((Collection<? extends String>) state.getProperty("roles"));
                            securityContext.setRoles(roles);
                            context.forward();
                        }
                    } catch (Throwable t) {
                        log.error("Error processing ResourceResponse", t);
                        context.replyWith(new DefaultResourceErrorResponse(req, ResourceErrorResponse.ErrorType.INTERNAL_ERROR));
                    }
                }
            });
        } catch (Throwable t) {
            log.error("Error initializing the security context", t);
            context.replyWith(new DefaultResourceErrorResponse(req, ResourceErrorResponse.ErrorType.INTERNAL_ERROR));
        }
    }

    private String getBearerToken(RequestContext requestContext) {
        String auth = requestContext.requestAttributes().getAttribute(HttpHeaders.Names.AUTHORIZATION, String.class);
        if (auth != null) {
            String[] a = auth.split(" ");
            if (a.length == 2 && a[0].equalsIgnoreCase(AUTH_TYPE)) {
                return a[1];
            }
        }
        return null;
    }

}
