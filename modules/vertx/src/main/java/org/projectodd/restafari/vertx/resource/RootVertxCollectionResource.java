package org.projectodd.restafari.vertx.resource;

import org.projectodd.restafari.spi.InitializationException;
import org.projectodd.restafari.spi.ResourceContext;
import org.projectodd.restafari.spi.resource.RootResource;

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
