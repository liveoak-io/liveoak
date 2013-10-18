package org.projectodd.restafari.container;

import org.projectodd.restafari.spi.*;
import org.projectodd.restafari.spi.resource.BlockingResource;
import org.projectodd.restafari.spi.resource.RootResource;

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
