package io.liveoak.keycloak;

import java.io.IOException;
import java.security.PublicKey;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import io.liveoak.spi.RequestContext;
import io.liveoak.spi.resource.SynchronousResource;
import io.liveoak.spi.resource.async.Resource;
import org.keycloak.VerificationException;
import org.keycloak.jose.jws.JWSInput;
import org.keycloak.jose.jws.crypto.RSAProvider;
import org.keycloak.representations.AccessToken;

/**
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 * @author Ken Finnigan
 */
public class TokenResource implements SynchronousResource {

    private final TokensResource parent;
    private KeycloakConfig config;
    private final String id;

    public TokenResource(String id, TokensResource parent, KeycloakConfig config) {
        this.parent = parent;
        this.config = config;
        this.id = id;
    }

    @Override
    public Resource parent() {
        return this.parent;
    }

    @Override
    public String id() {
        return this.id;
    }

    @Override
    public Map<String, ?> properties(RequestContext ctx) throws Exception {

        Map<String, Object> result = new HashMap<>();
        try {
            AccessToken token = parseToken(id);

            result.put("realm", token.getAudience());
            result.put("subject", token.getSubject());
            result.put("issued-at", new Date(token.getIssuedAt()));

            // Capture User info
            result.put("name", token.getName());
            result.put("given-name", token.getGivenName());
            result.put("family-name", token.getFamilyName());
            result.put("email", token.getEmail());

            Set<String> roles = new HashSet<>();

            AccessToken.Access realmAccess = token.getRealmAccess();
            if (realmAccess != null && realmAccess.getRoles() != null) {
                for (String r : realmAccess.getRoles()) {
                    roles.add(r);
                }
            }

            Map<String, AccessToken.Access> resourceAccess = token.getResourceAccess();
            if (resourceAccess != null) {
                for (Map.Entry<String, AccessToken.Access> e : resourceAccess.entrySet()) {
                    if (e.getValue().getRoles() != null) {
                        for (String r : e.getValue().getRoles()) {
                            roles.add(e.getKey().replace('/', '-') + "/" + r.replace('/', '-'));
                        }
                    }
                }
            }

            result.put("roles", roles);

        } catch (Throwable e) {
            result.put("error", e.getMessage());
        }
        return result;
    }

    private AccessToken parseToken(String tokenString) throws VerificationException {
        JWSInput input = new JWSInput(tokenString);

        AccessToken token;
        try {
            token = input.readJsonContent(AccessToken.class);
        } catch (IOException e) {
            throw new VerificationException(e);
        }

        PublicKey publicKey;
        try {
            publicKey = config.getPublicKey(token.getAudience());
        } catch (Exception e) {
            throw new VerificationException("Failed to get public key", e);
        }

        boolean verified = false;
        try {
            verified = RSAProvider.verify(input, publicKey);
        } catch (Exception ignore) {
        }
        if (!verified) throw new VerificationException("Token signature not validated");

        if (token.getSubject() == null) {
            throw new VerificationException("Token user was null");
        }

        if (!token.isActive()) {
            throw new VerificationException("Token is not active.");
        }

        return token;
    }

}
