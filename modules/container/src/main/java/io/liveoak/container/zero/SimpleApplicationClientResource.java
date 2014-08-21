package io.liveoak.container.zero;

import io.liveoak.spi.RequestContext;
import io.liveoak.spi.resource.SynchronousResource;
import io.liveoak.spi.resource.async.PropertySink;
import io.liveoak.spi.resource.async.Resource;
import io.liveoak.spi.resource.async.Responder;
import io.liveoak.spi.state.ResourceState;

/**
 * @author Ken Finnigan
 */
public class SimpleApplicationClientResource implements SynchronousResource {

    public SimpleApplicationClientResource(ApplicationClientsResource parent, ResourceState state) {
        this.parent = parent;
        this.id = (String) state.getProperty("id");
        this.type = (String) state.getProperty("type");
        this.securityKey = (String) state.getProperty("security-key");
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
        sink.accept("type", this.type);
        sink.accept("security-key", this.securityKey);
        sink.close();
    }

    @Override
    public void updateProperties(RequestContext ctx, ResourceState state, Responder responder) throws Exception {
        this.type = (String) state.getProperty("type");
        this.securityKey = (String) state.getProperty("security-key");
        responder.resourceUpdated(this);
    }

    @Override
    public void delete(RequestContext ctx, Responder responder) throws Exception {
        this.parent.delete(this);
        responder.resourceDeleted(this);
    }

    private ApplicationClientsResource parent;

    private String id;
    private String type;
    private String securityKey;
}
