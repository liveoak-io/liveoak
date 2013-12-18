/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.container.auth;

import io.liveoak.common.DefaultResourceErrorResponse;
import io.liveoak.common.DefaultSecurityContext;
import io.liveoak.spi.client.Client;
import io.liveoak.spi.client.ClientResourceResponse;
import io.liveoak.spi.ResourceErrorResponse;
import io.liveoak.spi.ResourceRequest;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.state.ResourceState;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.HttpHeaders;
import org.jboss.logging.Logger;

import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

/**
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
public class AuthHandler extends SimpleChannelInboundHandler<ResourceRequest> {

    public static final String AUTH_TYPE = "bearer";
    private static final Logger log = Logger.getLogger(AuthHandler.class);

    private Client client;

    public AuthHandler(Client client) {
        this.client = client;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ResourceRequest req) throws Exception {
        final RequestContext requestContext = req.requestContext();
        final DefaultSecurityContext securityContext = (DefaultSecurityContext) requestContext.securityContext();
        final String token = getBearerToken(requestContext);
        if (token != null) {
            initSecurityContext(ctx, req, securityContext, token);
        } else {
            ctx.fireChannelRead(req);
        }
    }

    private void initSecurityContext(final ChannelHandlerContext ctx, final ResourceRequest req, final DefaultSecurityContext securityContext, String token) {
        final RequestContext tokenRequestContext = new RequestContext.Builder().build();
        try {
            client.read(tokenRequestContext, "/auth/token-info/" + token, new Consumer<ClientResourceResponse>() {
                @Override
                public void accept(ClientResourceResponse resourceResponse) {
                    try {
                        ResourceState state = resourceResponse.state();
                        if (state.getProperty("error") != null) {
                            log.warn("Authentication failed. Request: " + req + ", error: " + state.getProperty("error"));
                            ctx.writeAndFlush(new DefaultResourceErrorResponse(req, ResourceErrorResponse.ErrorType.NOT_AUTHORIZED));
                        } else {
                            securityContext.setRealm((String) state.getProperty("realm"));
                            securityContext.setSubject((String) state.getProperty("subject"));
                            securityContext.setLastVerified(((Date) state.getProperty("issued-at")).getTime());

                            Set<String> roles = new HashSet<>();
                            roles.addAll((Collection<? extends String>) state.getProperty("roles"));
                            securityContext.setRoles(roles);
                            ctx.fireChannelRead(req);
                        }
                    } catch (Throwable t) {
                        ctx.writeAndFlush(new DefaultResourceErrorResponse(req, ResourceErrorResponse.ErrorType.INTERNAL_ERROR));
                    }
                }
            });
        } catch (Throwable t) {
            ctx.writeAndFlush(new DefaultResourceErrorResponse(req, ResourceErrorResponse.ErrorType.INTERNAL_ERROR));
        }
    }

    private String getBearerToken(RequestContext requestContext) {
        String auth = requestContext.requestAttributes().getAttribute(HttpHeaders.Names.AUTHORIZATION, String.class);
        if (auth != null) {
            String[] a = auth.split(" ");
            if (a.length == 2 && a[0].equals(AUTH_TYPE)) {
                return a[1];
            }
        }
        return null;
    }

}
