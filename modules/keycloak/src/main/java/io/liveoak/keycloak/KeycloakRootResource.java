package io.liveoak.keycloak;

import io.liveoak.spi.RequestContext;
import io.liveoak.spi.resource.RootResource;
import io.liveoak.spi.resource.async.Resource;
import io.liveoak.spi.resource.async.ResourceSink;
import io.liveoak.spi.resource.async.Responder;
import org.jboss.logging.Logger;

/**
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
public class KeycloakRootResource implements RootResource {

    private static final Logger log = Logger.getLogger("io.liveoak.keycloak");

    private Resource parent;
    private final String id;
    private final TokensResource tokensResource;

    public KeycloakRootResource(String id, KeycloakConfig address) {
        this.id = id;
        this.tokensResource = new TokensResource(this, address);
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
        if (id.equals(this.tokensResource.id())) {
            responder.resourceRead(this.tokensResource);
        } else {
            responder.noSuchResource(id);
        }
    }

    @Override
    public void readMembers(RequestContext ctx, ResourceSink sink) throws Exception {
        try {
            sink.accept(this.tokensResource);
        } catch (Throwable e) {
            sink.error(e);
        } finally {
            sink.close();
        }
    }

    public Logger logger() {
        return log;
    }
}

