package io.liveoak.container.resource;

import io.liveoak.spi.RequestContext;
import io.liveoak.spi.container.Server;
import io.liveoak.spi.resource.async.PropertySink;
import io.liveoak.spi.resource.async.Resource;

/**
 * @author Bob McWhirter
 */
public class ServerResource implements Resource {

    public ServerResource(Resource parent, String name, Server server) {
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
        sink.accept( "host", this.server.host().toString() );
        sink.accept( "port", this.server.port() );
        sink.close();
    }

    private Resource parent;
    private String name;
    private Server server;
}
