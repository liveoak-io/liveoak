/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.vertx.resource;

import io.liveoak.spi.InitializationException;
import io.liveoak.spi.ResourceContext;
import io.liveoak.spi.resource.RootResource;

/**
 * @author Bob McWhirter
 */
public class RootVertxCollectionResource extends VertxCollectionResource implements RootResource {

    public RootVertxCollectionResource( String id, String address ) {
        super( id, address );
    }

    @Override
    public void initialize( ResourceContext context ) throws InitializationException {
        vertx( context.vertx() );
    }

    @Override
    public void destroy() {
    }

}
