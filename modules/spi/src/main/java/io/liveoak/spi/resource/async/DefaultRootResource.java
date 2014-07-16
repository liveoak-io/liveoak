package io.liveoak.spi.resource.async;

import io.liveoak.spi.RequestContext;
import io.liveoak.spi.resource.RootResource;
import io.liveoak.spi.state.ResourceState;

/**
 * @author Bob McWhirter
 */
public class DefaultRootResource implements RootResource {

    public DefaultRootResource(String id) {
        this.id = id;
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
    public String id() {
        return this.id;
    }

    @Override
    public void updateProperties(RequestContext ctx, ResourceState state, Responder responder) throws Exception {
        responder.resourceUpdated(this);
    }

    private final String id;
    private Resource parent;
}
