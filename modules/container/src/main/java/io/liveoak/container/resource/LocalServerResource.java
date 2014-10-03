package io.liveoak.container.resource;

import java.util.HashMap;
import java.util.Map;

import io.liveoak.container.server.LocalServer;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.resource.SynchronousResource;
import io.liveoak.spi.resource.async.Resource;

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
    public Map<String, ?> properties(RequestContext ctx) throws Exception {
        Map<String, String> result = new HashMap<>();
        result.put("name", this.name);
        return result;
    }

    private Resource parent;
    private String name;
    private LocalServer server;
}
