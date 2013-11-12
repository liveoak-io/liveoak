package org.projectodd.restafari.container.auth;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.projectodd.restafari.container.DefaultRequestContext;
import org.projectodd.restafari.container.ResourceErrorResponse;
import org.projectodd.restafari.container.ResourceRequest;
import org.projectodd.restafari.security.impl.AuthServicesHolder;
import org.projectodd.restafari.security.impl.DefaultSecurityContext;
import org.projectodd.restafari.security.impl.SimpleLogger;
import org.projectodd.restafari.security.spi.AuthToken;
import org.projectodd.restafari.security.spi.AuthorizationRequestContext;
import org.projectodd.restafari.security.spi.AuthorizationService;
import org.projectodd.restafari.security.spi.TokenManager;
import org.projectodd.restafari.security.spi.TokenValidationException;
import org.projectodd.restafari.spi.RequestContext;
import org.projectodd.restafari.spi.SecurityContext;

/**
 * Handler for checking authorization of current request. It's independent of protocol. It delegates the work to {@link AuthorizationService}.
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class AuthorizationHandler extends SimpleChannelInboundHandler<ResourceRequest> {

    // TODO: replace with real logging
    private static final SimpleLogger log = new SimpleLogger(AuthorizationHandler.class);

    // TODO: Should be removed...
    static {
        try {
            AuthServicesHolder.getInstance().registerClassloader(AuthorizationHandler.class.getClassLoader());
            AuthServicesHolder.getInstance().registerDefaultPolicies();
        } catch (Throwable e) {
            log.error("Error occured during initialization of AuthorizationService", e);
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
            log.error("Exception occured in AuthorizationService check", e);
            throw e;
        }
    }

    protected void sendAuthorizationError(ChannelHandlerContext ctx, ResourceRequest req) {
        ctx.writeAndFlush( new ResourceErrorResponse( req, ResourceErrorResponse.ErrorType.NOT_AUTHORIZED) );
    }

    protected void establishSecurityContext(AuthToken token, RequestContext reqContext) {
        // Looks like a hack...
        if (reqContext instanceof DefaultRequestContext) {
            SecurityContext securityContext = DefaultSecurityContext.createFromAuthToken(token);
            ((DefaultRequestContext)reqContext).setSecurityContext(securityContext);
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
