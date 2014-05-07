package io.liveoak.spi.resource;

import java.net.URI;

import io.liveoak.spi.RequestContext;
import io.liveoak.spi.resource.async.PropertySink;
import io.liveoak.spi.resource.async.Resource;
import io.liveoak.spi.resource.async.ResourceSink;
import io.liveoak.spi.resource.async.Responder;
import io.liveoak.spi.state.ResourceState;

/**
 * @author Ken Finnigan
 */
public class DelegatingRootResource implements RootResource {

    public DelegatingRootResource(RootResource delegate) {
        this.delegate = delegate;
    }

    public RootResource delegate() {
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
    public void parent(Resource parent) {
        this.delegate.parent(parent);
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

    private final RootResource delegate;
}
