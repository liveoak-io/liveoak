package org.projectodd.restafari.security.impl;

import org.projectodd.restafari.security.spi.ApplicationMetadata;
import org.projectodd.restafari.security.spi.AuthPersister;
import org.projectodd.restafari.security.spi.AuthToken;
import org.projectodd.restafari.security.spi.TokenManager;
import org.projectodd.restafari.security.spi.TokenValidationException;
import org.projectodd.restafari.security.utils.RSAProvider;
import org.projectodd.restafari.spi.RequestContext;

/**
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class TokenManagerImpl implements TokenManager {

    @Override
    public AuthToken getAndValidateToken(RequestContext reqContext) throws TokenValidationException {
        JsonWebToken internalToken = getInternalToken(reqContext);

        if (internalToken != null) {
            validateToken(reqContext, internalToken);
            return new AuthTokenImpl(internalToken);
        } else {
            return AuthToken.ANONYMOUS_TOKEN;
        }
    }


    protected JsonWebToken getInternalToken(RequestContext requestContext) {
        String authorizationToken = requestContext.getRequestAttributes().getAttribute(AuthConstants.ATTR_AUTHORIZATION_TOKEN, String.class);

        // Use null token if Authorization header was not present
        if (authorizationToken == null) {
            return null;
        }

        return new JsonWebToken(authorizationToken);
    }

    protected void validateToken(RequestContext requestContext, JsonWebToken token) throws TokenValidationException {
        String targetApplicationId = AuthServicesHolder.getInstance().getApplicationIdResolver().resolveAppId(requestContext);

        AuthPersister authPersister = AuthServicesHolder.getInstance().getAuthPersister();
        ApplicationMetadata appMetadata = authPersister.getApplicationMetadata(targetApplicationId);

        try {
            RSAProvider.verify(token, appMetadata.getPublicKey());
        } catch (Exception e) {
            throw new TokenValidationException("Signature validation failed: " + e.getMessage(), e);
        }

        JsonWebToken.Claims claims = token.getClaims();
        if (!claims.isActive()) {
            throw new TokenValidationException("Token is not active.");
        }
        String user = claims.getSubject();
        if (user == null) {
            throw new TokenValidationException("Token user was null");
        }

        // Check if applicationName from token corresponds with registered applicationName from metadata
        if (!appMetadata.getApplicationName().equals(claims.getIssuedFor())) {
            throw new TokenValidationException("Target application from token doesn't match with registered application name. ApplicationName from metadata: " +
                    appMetadata.getApplicationName() + ", applicationName from token: " + claims.getIssuedFor());
        }

        // Check if realmName from token corresponds with registered realmName from metadata
        if (!appMetadata.getRealmName().equals(claims.getAudience())) {
            throw new TokenValidationException("Token audience doesn't match. RealmName from metadata: " +
                    appMetadata.getRealmName() + ", applicationName from token: " + claims.getAudience());
        }
    }


}
