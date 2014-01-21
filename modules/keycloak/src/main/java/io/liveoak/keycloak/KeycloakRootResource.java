package io.liveoak.keycloak;

import io.liveoak.spi.RequestContext;
import io.liveoak.spi.resource.RootResource;
import io.liveoak.spi.resource.async.Resource;
import io.liveoak.spi.resource.async.Responder;
import org.jboss.logging.Logger;
import org.keycloak.models.RealmModel;

/**
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
public class KeycloakRootResource implements RootResource {

    private static final Logger log = Logger.getLogger("io.liveoak.keycloak");

    private Resource parent;
    private final String id;
    private final TokensResource tokensResource;
    private final PublicKeyResource publicKeyResource;

    public KeycloakRootResource(String id, RealmModel realmModel) {
        this.id = id;
        this.tokensResource = new TokensResource( this, realmModel );
        this.publicKeyResource = new PublicKeyResource( this, realmModel );
    }

    @Override
    public String id() {
        return id;
    }

    @Override
    public void parent(Resource parent) {
        this.parent = parent;
    }

    @Override
    public Resource parent() {
        return this.parent;
    }

    @Override
    public void readMember(RequestContext ctx, String id, Responder responder) {
        if ( id.equals( this.tokensResource.id() ) ) {
            responder.resourceRead( this.tokensResource );
        } else if ( id.equals( this.publicKeyResource.id() ) ) {
            responder.resourceRead( this.publicKeyResource );
        } else {
            responder.noSuchResource( id );
        }
    }

    public Logger logger() {
        return log;
    };
}

