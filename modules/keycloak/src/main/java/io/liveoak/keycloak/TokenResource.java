package io.liveoak.keycloak;

import io.liveoak.spi.RequestContext;
import io.liveoak.spi.resource.async.PropertySink;
import io.liveoak.spi.resource.async.Resource;
import org.keycloak.RSATokenVerifier;
import org.keycloak.VerificationException;
import org.keycloak.models.RealmModel;
import org.keycloak.representations.JsonWebToken;
import org.keycloak.representations.SkeletonKeyToken;

import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author Bob McWhirter
 */
public class TokenResource implements Resource {


    public TokenResource(Resource parent, String id, RealmModel realmModel) {
        this.parent = parent;
        this.id = id;
        this.realmModel = realmModel;
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
            SkeletonKeyToken token = RSATokenVerifier.verifyToken(id, realmModel.getPublicKey(), realmModel.getName());

            sink.accept("realm", token.getAudience());
            sink.accept("subject", token.getSubject());
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
            sink.accept( "error", t.getMessage());
        }
        sink.close();

    }

    private final Resource parent;
    private final String id;
    private final RealmModel realmModel;
}
