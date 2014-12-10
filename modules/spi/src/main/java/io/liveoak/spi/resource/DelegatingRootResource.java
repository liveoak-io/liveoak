package io.liveoak.spi.resource;

import io.liveoak.spi.resource.async.DelegatingResource;
import io.liveoak.spi.resource.async.Resource;

/**
 * @author Ken Finnigan
 */
public class DelegatingRootResource extends DelegatingResource implements RootResource {

    public DelegatingRootResource(RootResource delegate) {
        super(delegate);
        this.delegate = delegate;
    }

    public RootResource delegate() {
        return this.delegate;
    }

    @Override
    public void parent(Resource parent) {
        this.delegate.parent(parent);
    }

    private final RootResource delegate;
}
