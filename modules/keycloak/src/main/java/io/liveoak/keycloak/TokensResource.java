package io.liveoak.keycloak;

import io.liveoak.spi.RequestContext;
import io.liveoak.spi.resource.async.Resource;
import io.liveoak.spi.resource.async.Responder;

import java.security.PublicKey;
import java.util.Hashtable;
import java.util.Map;

/**
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
public class TokensResource implements Resource {

    private Resource parent;
    private KeycloakConfig config;
    private Map<String, PublicKey> publicKeys;

    public TokensResource(Resource parent, KeycloakConfig config) {
        this.parent = parent;
        this.config = config;
        this.publicKeys = new Hashtable<>();
    }

    @Override
    public Resource parent() {
        return this.parent;
    }

    @Override
    public String id() {
        return "token-info";
    }

    @Override
    public void readMember(RequestContext ctx, String id, Responder responder) throws Exception {
        responder.resourceRead(new TokenResource(id, this, config));
    }

}
