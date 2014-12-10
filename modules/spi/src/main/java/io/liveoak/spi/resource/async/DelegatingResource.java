package io.liveoak.spi.resource.async;

import java.net.URI;

import io.liveoak.spi.RequestContext;
import io.liveoak.spi.state.ResourceState;

/**
 * @author Ken Finnigan
 */
public class DelegatingResource implements Resource {

    public DelegatingResource(Resource delegate) {
        this.delegate = delegate;
    }

    public Resource delegate() {
        return this.delegate;
    }

    @Override
    public Resource parent() {
        return this.delegate.parent();
    }

    @Override
    public String id() {
        return this.delegate.id();
    }

    @Override
    public URI uri() {
        return this.delegate.uri();
    }

    @Override
    public void readProperties(RequestContext ctx, PropertySink sink) throws Exception {
        this.delegate.readProperties(ctx, sink);
    }

    @Override
    public void updateProperties(RequestContext ctx, ResourceState state, Responder responder) throws Exception {
        this.delegate.updateProperties(ctx, state, responder);
    }

    @Override
    public void createMember(RequestContext ctx, ResourceState state, Responder responder) throws Exception {
        this.delegate.createMember(ctx, state, responder);
    }

    @Override
    public void readMembers(RequestContext ctx, ResourceSink sink) throws Exception {
        this.delegate.readMembers(ctx, sink);
    }

    @Override
    public void readMember(RequestContext ctx, String id, Responder responder) throws Exception {
        this.delegate.readMember(ctx, id, responder);
    }

    @Override
    public void delete(RequestContext ctx, Responder responder) throws Exception {
        this.delegate.delete(ctx, responder);
    }

    private final Resource delegate;
}
