package io.liveoak.container.resource;

import java.util.HashMap;
import java.util.Map;

import io.liveoak.spi.RequestContext;
import io.liveoak.spi.container.NetworkServer;
import io.liveoak.spi.resource.SynchronousResource;
import io.liveoak.spi.resource.async.Resource;

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
    public Map<String, ?> properties(RequestContext ctx) throws Exception {
        Map<String, Object> result = new HashMap<>();
        result.put("name", this.name);
        result.put("host", this.server.host().toString());
        result.put("port", this.server.port());
        return result;
    }

    private Resource parent;
    private String name;
    private NetworkServer server;
}
