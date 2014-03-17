package io.liveoak.spi.resource.config;

import io.liveoak.spi.RequestContext;
import io.liveoak.spi.resource.RootResource;
import io.liveoak.spi.resource.async.PropertySink;
import io.liveoak.spi.resource.async.Resource;
import io.liveoak.spi.resource.async.Responder;
import io.liveoak.spi.state.ResourceState;

/**
 * @author <a href="http://community.jboss.org/people/kenfinni">Ken Finnigan</a>
 */
public class WrappingConfigResource implements RootResource, ConfigResource {

    private final String id;
    private Resource parent;
    private final Resource configurable;

    public WrappingConfigResource(String id, Resource configurable) {
        this.id = id;
        this.configurable = configurable;
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
    public String id() {
        return this.id;
    }

    @Override
    public void readProperties(RequestContext ctx, PropertySink sink) throws Exception {
        readConfigProperties(ctx, sink, this.configurable);
        sink.close();
    }

    @Override
    public void updateProperties(RequestContext ctx, ResourceState state, Responder responder) throws Exception {
        updateConfigProperties(ctx, state, responder, this.configurable);
        responder.resourceUpdated(this);
    }

}
