package io.liveoak.scripts.endpoints;

import io.liveoak.spi.RequestContext;
import io.liveoak.spi.resource.async.PropertySink;
import io.liveoak.spi.resource.async.Resource;

/**
 * Scripts to be exposed to client via a REST endpoint.
 *
 * TODO: implement features
 *
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 */
public class EndpointScripts implements Resource {

    private Resource parent;
    private static final String ID = "endpoint-scripts";

    public EndpointScripts(Resource parent) {
        this.parent = parent;
    }

    @Override
    public Resource parent() {
        return parent;
    }

    @Override
    public String id() {
        return ID;
    }

    @Override
    public void readProperties(RequestContext ctx, PropertySink sink) throws Exception {
        sink.accept("name", "Endpoint Scripts");
        sink.accept("description", "Scripts to be exposed to clients via a custom REST endpoint");
        sink.accept("count", 0);
        sink.close();
    }
}
