package io.liveoak.vertx.resource;

import io.liveoak.spi.InitializationException;
import io.liveoak.spi.ResourceContext;
import io.liveoak.spi.resource.RootResource;

/**
 * @author Bob McWhirter
 */
public class RootVertxCollectionResource extends VertxCollectionResource implements RootResource {

    public RootVertxCollectionResource(String id, String address) {
        super( id, address );
    }

    @Override
    public void initialize(ResourceContext context) throws InitializationException {
        vertx( context.vertx() );
    }

    @Override
    public void destroy() {
    }

}
