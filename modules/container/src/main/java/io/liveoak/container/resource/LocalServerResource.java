package io.liveoak.container.resource;

import io.liveoak.common.codec.DefaultResourceState;
import io.liveoak.container.server.LocalServer;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.resource.SynchronousResource;
import io.liveoak.spi.resource.async.Resource;
import io.liveoak.spi.state.ResourceState;

/**
 * @author Bob McWhirter
 */
public class LocalServerResource implements SynchronousResource {

    public LocalServerResource(Resource parent, String name, LocalServer server) {
        this.parent = parent;
        this.name = name;
        this.server = server;
    }

    @Override
    public Resource parent() {
        return this.parent;
    }

    @Override
    public String id() {
        return this.name;
    }

    @Override
    public ResourceState properties(RequestContext ctx) throws Exception {
        ResourceState result = new DefaultResourceState();
        result.putProperty("name", this.name);
        return result;
    }

    private Resource parent;
    private String name;
    private LocalServer server;
}
