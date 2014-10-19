/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.spi.resource;

import io.liveoak.spi.RequestContext;
import io.liveoak.spi.resource.async.Resource;
import io.liveoak.spi.resource.async.Responder;
import io.liveoak.spi.state.ResourceState;

/**
 * A root resource capable of being registered with a container.
 *
 * @author Bob McWhirter
 * @author Ken Finnigan
 */
public interface RootResource extends Resource {

    /** Called when the resource gets mounted.
     *
     * @param parent The mount-point parent.
     */
    void parent(Resource parent);

    default void start() throws Exception {
    }

    default void stop() {
    }

    /**
     * Initialize this object's state. Only called on container startup.
     *
     * @param state     The inbound representation of the state.
     * @param responder To respond to the action.
     */
    default void initializeProperties(RequestContext ctx, ResourceState state, Responder responder) throws Exception {
        updateProperties(ctx, state, responder);
    }
}
