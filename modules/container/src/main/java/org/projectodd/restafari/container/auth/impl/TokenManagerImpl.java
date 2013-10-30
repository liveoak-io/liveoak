package org.projectodd.restafari.container.auth.impl;

import org.projectodd.restafari.container.ResourceRequest;
import org.projectodd.restafari.container.auth.spi.*;
import org.projectodd.restafari.container.auth.utils.RSAProvider;

/**
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class TokenManagerImpl implements TokenManager {

    @Override
    public JsonWebToken getToken(ResourceRequest resourceRequest) {
        String authorizationToken = resourceRequest.authorizationToken();

        // Use null token if Authorization header was not present
        if (authorizationToken == null) {
            return null;
        }

        return new JsonWebToken(authorizationToken);
    }

    @Override
    public void validateToken(ResourceRequest resourceRequest, JsonWebToken token) throws TokenValidationException {
        String targetApplicationId = AuthServicesHolder.getInstance().getApplicationIdResolver().resolveAppId(resourceRequest);

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
