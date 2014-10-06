package io.liveoak.mongo.config;

import java.net.URI;

import com.mongodb.ServerAddress;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.resource.async.PropertySink;
import io.liveoak.spi.resource.async.Resource;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 */
public class ServerAddressResource implements Resource {

    RootMongoConfigResource parent;
    ServerAddress serverAddress;

    public ServerAddressResource(RootMongoConfigResource parent, ServerAddress serverAddress) {
        this.parent = parent;
        this.serverAddress = serverAddress;
    }

    @Override
    public Resource parent() {
        return parent;
    }

    @Override
    public String id() {
        return null;
    }

    @Override
    public URI uri() {
        return null;
    }

    @Override
    public void readProperties(RequestContext ctx, PropertySink sink) throws Exception {
        sink.accept("host", serverAddress.getHost());
        sink.accept("port", serverAddress.getPort());
        sink.complete();
    }
}
