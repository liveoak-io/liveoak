/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.keycloak.interceptor;

import io.liveoak.common.DefaultResourceErrorResponse;
import io.liveoak.common.security.DefaultSecurityContext;
import io.liveoak.common.security.SecurityHelper;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.ResourceErrorResponse;
import io.liveoak.spi.ResourcePath;
import io.liveoak.spi.ResourceRequest;
import io.liveoak.spi.client.Client;
import io.liveoak.spi.container.interceptor.DefaultInterceptor;
import io.liveoak.spi.container.interceptor.InboundInterceptorContext;
import io.netty.handler.codec.http.HttpHeaders;
import org.jboss.logging.Logger;

/**
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 * @author Ken Finnigan
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
        String prefix = getPrefix(req.resourcePath());

        SecurityHelper.auth(client, securityContext, prefix, token,
                // Success function
                () -> context.forward(),
                // No Such Resource function
                () -> {
                    log.info("Auth not configured for " + prefix);
                    context.forward();
                },
                // Not Authorized function
                error -> {
                    log.warn("Authentication failed. Request: " + req + ", error: " + error);
                    context.replyWith(new DefaultResourceErrorResponse(req, ResourceErrorResponse.ErrorType.NOT_AUTHORIZED));
                },
                // Handle throwable
                throwable -> {
                    log.error("Error processing authentication", throwable);
                    context.replyWith(new DefaultResourceErrorResponse(req, ResourceErrorResponse.ErrorType.INTERNAL_ERROR));
                });
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
