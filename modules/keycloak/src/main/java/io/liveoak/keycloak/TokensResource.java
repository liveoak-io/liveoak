package io.liveoak.keycloak;

import io.liveoak.spi.RequestContext;
import io.liveoak.spi.resource.async.Resource;
import io.liveoak.spi.resource.async.Responder;
import org.keycloak.models.RealmModel;

/**
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
public class TokensResource implements Resource {

    private Resource parent;
    private final RealmModel realmModel;

    public TokensResource(Resource parent, RealmModel realmModel) {
        this.parent = parent;
        this.realmModel = realmModel;
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
        responder.resourceRead(new TokenResource(this, id, realmModel) );
    }
}
