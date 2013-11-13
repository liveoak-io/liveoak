package io.liveoak.container;

import io.liveoak.spi.*;
import io.liveoak.spi.resource.RootResource;

public class InMemoryDBResource extends InMemoryCollectionResource implements RootResource {

    public InMemoryDBResource(String id) {
        super( null, id );
    }

    @Override
    public void initialize(ResourceContext context) throws InitializationException {
    }

    @Override
    public void destroy() {
    }

}
