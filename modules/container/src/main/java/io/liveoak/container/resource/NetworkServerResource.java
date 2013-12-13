package io.liveoak.container.resource;

import io.liveoak.spi.RequestContext;
import io.liveoak.spi.container.NetworkServer;
import io.liveoak.spi.container.Server;
import io.liveoak.spi.resource.async.PropertySink;
import io.liveoak.spi.resource.async.Resource;

/**
 * @author Bob McWhirter
 */
public class NetworkServerResource implements Resource {

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
    public void readProperties(RequestContext ctx, PropertySink sink) throws Exception {
        sink.accept( "name", this.name );
        sink.accept( "host", this.server.host().toString() );
        sink.accept( "port", this.server.port() );
        sink.close();
    }

    private Resource parent;
    private String name;
    private NetworkServer server;
}
