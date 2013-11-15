/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
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
