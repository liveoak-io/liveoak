/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.spi.resource.async;

import io.liveoak.spi.MediaType;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.state.BinaryResourceState;

/**
 * @author Bob McWhirter
 * @author <a href="http://community.jboss.org/people/kenfinni">Ken Finnigan</a>
 */
public interface BinaryResource extends Resource {

    /**
     * Retrieve the {@link MediaType} of the binary resource.
     *
     * @return The {@link MediaType} of the binary resource.
     */
    MediaType mediaType();

    /**
     * Write the content of this object to the provided sink.
     *
     * @param sink The sink to stream content to.
     */
    void readContent(RequestContext ctx, BinaryContentSink sink) throws Exception;

    /**
     * Update this object's content.
     *
     * @param state     The inbound binary representation of the content.
     * @param responder To respond to the action.
     */
    default void updateContent(RequestContext ctx, BinaryResourceState state, Responder responder) throws Exception {
        responder.updateNotSupported(this);
    }
}
