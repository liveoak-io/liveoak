package io.liveoak.container;

import io.liveoak.spi.RequestContext;
import io.liveoak.spi.client.Client;
import io.liveoak.spi.resource.BlockingResource;
import io.liveoak.spi.resource.RootResource;
import io.liveoak.spi.resource.SynchronousResource;
import io.liveoak.spi.resource.async.Resource;
import io.liveoak.spi.state.ResourceState;

/**
 * @author Bob McWhirter
 */
public class BlockingProxyResource implements RootResource, BlockingResource, SynchronousResource {
    private final String id;
    private Client client;
    private Resource parent;

    public BlockingProxyResource(String id, Client client) {
        this.id = id;
        this.client = client;
    }

    @Override
    public String id() {
        return this.id;
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
    public ResourceState properties(RequestContext ctx) throws Exception {
        RequestContext requestContext = new RequestContext.Builder().build();
        ResourceState result = client.read(requestContext, "/testApp/db/people/blockingproxybob");
        return result;
    }

}
