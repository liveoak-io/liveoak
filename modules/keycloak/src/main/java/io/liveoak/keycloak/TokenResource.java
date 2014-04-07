package io.liveoak.keycloak;

import io.liveoak.spi.RequestContext;
import io.liveoak.spi.resource.async.PropertySink;
import io.liveoak.spi.resource.async.Resource;
import org.keycloak.VerificationException;
import org.keycloak.jose.jws.JWSInput;
import org.keycloak.jose.jws.crypto.RSAProvider;
import org.keycloak.representations.AccessToken;

import java.io.IOException;
import java.security.PublicKey;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
public class TokenResource implements Resource {

    private final TokensResource parent;
    private KeycloakConfig config;
    private final String id;

    public  TokenResource(String id, TokensResource parent, KeycloakConfig config) {
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
    public void readProperties(RequestContext ctx, PropertySink sink) throws Exception {

        try {
            AccessToken token = parseToken(id);

            sink.accept("realm", token.getAudience());
            sink.accept("subject", token.getSubject());
            sink.accept("issued-at", new Date(token.getIssuedAt()));

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

            sink.accept("roles", roles);
        } catch (Throwable t) {
            sink.accept("error", t.getMessage());
        }
        sink.close();

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
