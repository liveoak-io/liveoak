package io.liveoak.keycloak;

import org.jboss.resteasy.jose.jws.JWSBuilder;
import org.jboss.resteasy.jwt.JsonSerialization;
import org.keycloak.models.KeycloakSession;
import org.keycloak.representations.SkeletonKeyToken;

import java.security.PrivateKey;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

/**
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
public class TokenUtil {

    private String realm;

    private PrivateKey privateKey;

    public TokenUtil(KeycloakRootResource keycloak) {
        realm = keycloak.getRealm();

        KeycloakSession session = keycloak.createSession();
        try {
            privateKey = session.getRealm(keycloak.getRealm()).getPrivateKey();
        } finally {
            session.close();
        }
    }

    public SkeletonKeyToken createToken() {
        SkeletonKeyToken token = new SkeletonKeyToken();
        token.id("token-id");
        token.principal("user-id");
        token.audience(realm);
        token.expiration(System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(300));
        token.issuedFor("app-id");
        token.issuedNow();

        token.setRealmAccess(new SkeletonKeyToken.Access().roles(Collections.singleton("realm-role")));
        token.addAccess("app-id").roles(Collections.singleton("app-role"));
        token.addAccess("app2-id").roles(Collections.singleton("app-role"));

        return token;
    }

    public String toString(SkeletonKeyToken token) throws Exception {
        byte[] tokenBytes = JsonSerialization.toByteArray(token, false);
        return new JWSBuilder().content(tokenBytes).rsa256(privateKey);
    }


}
