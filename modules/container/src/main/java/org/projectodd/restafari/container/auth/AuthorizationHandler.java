package org.projectodd.restafari.container.auth;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.projectodd.restafari.container.ResourceErrorResponse;
import org.projectodd.restafari.container.ResourceRequest;
import org.projectodd.restafari.security.impl.AuthServicesHolder;
import org.projectodd.restafari.security.impl.SimpleLogger;
import org.projectodd.restafari.security.spi.AuthorizationRequestContext;
import org.projectodd.restafari.security.spi.AuthorizationService;
import org.projectodd.restafari.security.spi.JsonWebToken;
import org.projectodd.restafari.security.spi.TokenManager;
import org.projectodd.restafari.security.spi.TokenValidationException;
import org.projectodd.restafari.spi.RequestContext;

/**
 * Handler for checking authorization of current request. It's independent of protocol. It delegates the work to {@link AuthorizationService}.
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class AuthorizationHandler extends SimpleChannelInboundHandler<ResourceRequest> {

    // TODO: Should be removed...
    static {
        AuthServicesHolder.getInstance().registerClassloader(AuthorizationHandler.class.getClassLoader());
        AuthServicesHolder.getInstance().registerDefaultPolicies();
    }

    // TODO: replace with real logging
    private final SimpleLogger log = new SimpleLogger(AuthorizationHandler.class);

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ResourceRequest req) throws Exception {
        JsonWebToken token;
        AuthorizationService authService = AuthServicesHolder.getInstance().getAuthorizationService();
        TokenManager tokenManager = AuthServicesHolder.getInstance().getTokenManager();
        RequestContext reqContext = req.requestContext();

        try {
            token = tokenManager.getToken(reqContext);

            // Validation is not done for null token. Null token is allowed in case of public requests
            if (token != null) {
                tokenManager.validateToken(reqContext, token);
            }
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
            // TODO: Attached current token and principal to requestContext
            ctx.fireChannelRead(req);
        } else {
            sendAuthorizationError(ctx, req);
        }
    }

    protected void sendAuthorizationError(ChannelHandlerContext ctx, ResourceRequest req) {
        ctx.writeAndFlush( new ResourceErrorResponse( req, ResourceErrorResponse.ErrorType.NOT_AUTHORIZED) );
    }
}
