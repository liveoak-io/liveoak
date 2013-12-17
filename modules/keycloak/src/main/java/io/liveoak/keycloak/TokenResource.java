package io.liveoak.keycloak;

import io.liveoak.spi.RequestContext;
import io.liveoak.spi.resource.async.PropertySink;
import io.liveoak.spi.resource.async.Resource;
import io.liveoak.spi.resource.async.Responder;
import org.keycloak.RSATokenVerifier;
import org.keycloak.VerificationException;
import org.keycloak.representations.SkeletonKeyToken;

import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
public class TokenResource implements Resource {

    private KeycloakRootResource keycloak;

    public TokenResource(KeycloakRootResource keycloak) {
        this.keycloak = keycloak;
    }

    @Override
    public Resource parent() {
        return keycloak;
    }

    @Override
    public String id() {
        return "token-info";
    }

    @Override
    public void readMember(RequestContext ctx, String id, Responder responder) {
        responder.resourceRead(new Resource() {
            @Override
            public Resource parent() {
                return TokenResource.this;
            }

            @Override
            public String id() {
                return id;
            }

            @Override
            public void readProperties(RequestContext ctx, PropertySink sink) throws Exception {
                try {
                    SkeletonKeyToken token = RSATokenVerifier.verifyToken(id, keycloak.getPublicKey(), keycloak.getRealm());
                    sink.accept("realm", token.getAudience());
                    sink.accept("subject", token.getPrincipal());
                    sink.accept("issued-at", new Date(token.getIssuedAt()));

                    Set<String> roles = new HashSet<>();

                    SkeletonKeyToken.Access realmAccess = token.getRealmAccess();
                    if (realmAccess != null && realmAccess.getRoles() != null) {
                        for (String r : realmAccess.getRoles()) {
                            roles.add(r);
                        }
                    }

                    Map<String, SkeletonKeyToken.Access> resourceAccess = token.getResourceAccess();
                    if (resourceAccess != null) {
                        for (Map.Entry<String, SkeletonKeyToken.Access> e : resourceAccess.entrySet()) {
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
        });
    }
}
