package io.liveoak.container.resource;

import io.liveoak.common.codec.DefaultResourceState;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.container.NetworkServer;
import io.liveoak.spi.resource.SynchronousResource;
import io.liveoak.spi.resource.async.Resource;
import io.liveoak.spi.state.ResourceState;

/**
 * @author Bob McWhirter
 */
public class NetworkServerResource implements SynchronousResource {

    public NetworkServerResource(Resource parent, String name, NetworkServer server) {
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
        result.putProperty("host", this.server.host().toString());
        result.putProperty("port", this.server.port());
        return result;
    }

    private Resource parent;
    private String name;
    private NetworkServer server;
}
