/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.container.auth;

import io.liveoak.container.DefaultRequestContext;
import io.liveoak.container.ResourceErrorResponse;
import io.liveoak.container.ResourceRequest;
import io.liveoak.security.impl.AuthServicesHolder;
import io.liveoak.security.impl.DefaultSecurityContext;
import io.liveoak.security.spi.AuthToken;
import io.liveoak.security.spi.AuthorizationRequestContext;
import io.liveoak.security.spi.AuthorizationService;
import io.liveoak.security.spi.TokenManager;
import io.liveoak.security.spi.TokenValidationException;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.SecurityContext;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.jboss.logging.Logger;

/**
 * Handler for checking authorization of current request. It's independent of protocol. It delegates the work to {@link AuthorizationService}.
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class AuthorizationHandler extends SimpleChannelInboundHandler<ResourceRequest> {

    // TODO: replace with real logging
    private static final Logger log = Logger.getLogger(AuthorizationHandler.class);

    // TODO: Should be removed...
    static {
        try {
            AuthServicesHolder.getInstance().registerClassloader(AuthorizationHandler.class.getClassLoader());
            AuthServicesHolder.getInstance().registerDefaultPolicies();
        } catch (Throwable e) {
            log.error("Error occurred during initialization of AuthorizationService", e);
            throw e;
        }
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ResourceRequest req) throws Exception {
        try {
            AuthToken token;
            AuthorizationService authService = AuthServicesHolder.getInstance().getAuthorizationService();
            TokenManager tokenManager = AuthServicesHolder.getInstance().getTokenManager();
            RequestContext reqContext = req.requestContext();

            try {
                token = tokenManager.getAndValidateToken(reqContext);
            } catch (TokenValidationException e) {
                String message = "Error when obtaining token: " + e.getMessage();
                log.warn(message);
                if (log.isTraceEnabled()) {
                    log.trace(message, e);
                }

                sendAuthorizationError(ctx, req);
                return;
            }

            if (authService.isAuthorized(new AuthorizationRequestContext(token, reqContext))) {
                establishSecurityContext(token, reqContext);
                ctx.fireChannelRead(req);
            } else {
                sendAuthorizationError(ctx, req);
            }
        } catch (Throwable e) {
            log.error("Exception occurred in AuthorizationService check", e);
            throw e;
        }
    }

    protected void sendAuthorizationError(ChannelHandlerContext ctx, ResourceRequest req) {
        ctx.writeAndFlush(new ResourceErrorResponse(req, ResourceErrorResponse.ErrorType.NOT_AUTHORIZED));
    }

    protected void establishSecurityContext(AuthToken token, RequestContext reqContext) {
        // Looks like a hack...
        if (reqContext instanceof DefaultRequestContext) {
            SecurityContext securityContext = DefaultSecurityContext.createFromAuthToken(token);
            ((DefaultRequestContext) reqContext).setSecurityContext(securityContext);
        } else {
            log.warn("Can't establish securityContext to RequestContext " + reqContext);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        super.exceptionCaught(ctx, cause);
    }
}
