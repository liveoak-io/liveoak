package org.projectodd.restafari.container.auth;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.projectodd.restafari.container.ResourceErrorResponse;
import org.projectodd.restafari.container.ResourceRequest;
import org.projectodd.restafari.container.auth.service.AuthorizationRequestContext;
import org.projectodd.restafari.container.auth.service.AuthorizationService;
import org.projectodd.restafari.container.auth.service.DemoAuthorizationService;
import org.projectodd.restafari.container.auth.utils.PemUtils;
import org.projectodd.restafari.container.auth.utils.RSAProvider;

import java.security.PublicKey;

/**
 * Handler for checking authorization of current request. It's independent of protocol. It delegates the work to {@link AuthorizationService}.
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class AuthorizationHandler extends SimpleChannelInboundHandler<ResourceRequest> {

    // TODO: Drop this
    private static final String DEFAULT_PUBLIC_KEY = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCrVrCuTtArbgaZzL1hvh0xtL5mc7o0NqPVnYXkLvgcwiC3BjLGw1tGEGoJaXDuSaRllobm53JBhjx33UNv+5z/UMG4kytBWxheNVKnL6GgqlNabMaFfPLPCF8kAgKnsi79NMo+n6KnSY8YeUmec/p2vjO2NjsSAVcWEQMVhJ31LwIDAQAB";

    private final PublicKey realmPublicKey;
    // TODO: Better way to initialize AuthorizationService. Maybe from DefaultContainer?
    private final AuthorizationService authService = new DemoAuthorizationService();

    // TODO: replace with real logging
    private final boolean verboseLogging;

    public AuthorizationHandler() {
        // TODO: Better way to initialize this (not during each constructor call)
        String realmPublicKey = System.getProperty("mbaas.realmPublicKey", DEFAULT_PUBLIC_KEY);
        try {
            this.realmPublicKey = PemUtils.decodePublicKey(realmPublicKey);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        verboseLogging = Boolean.getBoolean("mbaas.auth.verbose");
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ResourceRequest req) throws Exception {
        JsonWebToken token;
        try {
            token = getJsonWebToken(req.authorizationToken(), req.resourcePath().head());
        } catch (Exception e) {
            String message = "Error when obtaining token: " + e.getMessage();

            if (verboseLogging) {
                e.printStackTrace();
            }

            sendAuthorizationError(ctx, req);
            return;
        }

        if (authService.isAuthorized(new AuthorizationRequestContext(token, req))) {
            ctx.fireChannelRead(req);
        } else {
            sendAuthorizationError(ctx, req);
        }
    }

    protected void sendAuthorizationError(ChannelHandlerContext ctx, ResourceRequest req) {
        ctx.writeAndFlush( new ResourceErrorResponse( req, ResourceErrorResponse.ErrorType.NOT_AUTHORIZED) );
    }

    protected JsonWebToken getJsonWebToken(String authorizationToken, String requestAudience) {
        // Use null token if Authorization header was not present
        if (authorizationToken == null) {
            return null;
        }

        JsonWebToken token = new JsonWebToken(authorizationToken);
        verifyJsonWebToken(token, requestAudience);
        return token;
    }

    protected void verifyJsonWebToken(JsonWebToken token, String requestAudience) {
        try {
            RSAProvider.verify(token, realmPublicKey);
        } catch (Exception e) {
            throw new IllegalStateException("Signature validation failed: " + e.getMessage(), e);
        }

        JsonWebToken.Claims claims = token.getClaims();
        if (!claims.isActive()) {
            throw new IllegalStateException("Token is not active.");
        }
        String user = claims.getSubject();
        if (user == null) {
            throw new IllegalStateException("Token user was null");
        }

        if (!requestAudience.equals(claims.getAudience())) {
            throw new IllegalStateException("Token audience doesn't match domain");

        }
    }
}
