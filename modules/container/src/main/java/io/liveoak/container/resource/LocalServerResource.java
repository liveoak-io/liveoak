package io.liveoak.container.resource;

import io.liveoak.container.server.LocalServer;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.resource.async.PropertySink;
import io.liveoak.spi.resource.async.Resource;

/**
 * @author Bob McWhirter
 */
public class LocalServerResource implements Resource {

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
    public void readProperties(RequestContext ctx, PropertySink sink) throws Exception {
        sink.accept("name", this.name);
        sink.close();
    }

    private Resource parent;
    private String name;
    private LocalServer server;
}
