/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.spi.resource;

import io.liveoak.spi.InitializationException;
import io.liveoak.spi.ResourceContext;
import io.liveoak.spi.resource.async.Resource;

/**
 * A root resource capable of being registered with a container.
 *
 * @author Bob McWhirter
 */
public interface RootResource extends Resource {

    /**
     * Initialize the resource.
     *
     * @param context The initialization context.
     * @throws io.liveoak.spi.InitializationException
     *          If an error occurs.
     */
    void initialize(ResourceContext context) throws InitializationException;

    /**
     * Free resources used by the resource and shutdown.
     */
    void destroy();


    default Resource parent() {
        return null;
    }
}
