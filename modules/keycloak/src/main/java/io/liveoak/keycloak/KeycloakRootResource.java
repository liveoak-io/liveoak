package io.liveoak.keycloak;

import java.util.Collection;
import java.util.LinkedList;

import io.liveoak.spi.RequestContext;
import io.liveoak.spi.resource.RootResource;
import io.liveoak.spi.resource.SynchronousResource;
import io.liveoak.spi.resource.async.Resource;
import org.jboss.logging.Logger;

/**
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
public class KeycloakRootResource implements RootResource, SynchronousResource {

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
    public Resource member(RequestContext ctx, String id) {
        if (id.equals(this.tokensResource.id())) {
            return this.tokensResource;
        }
        return null;
    }

    @Override
    public Collection<Resource> members(RequestContext ctx) throws Exception {
        LinkedList<Resource> members = new LinkedList<>();
        members.add(this.tokensResource);
        return members;
    }

    public Logger logger() {
        return log;
    }
}

