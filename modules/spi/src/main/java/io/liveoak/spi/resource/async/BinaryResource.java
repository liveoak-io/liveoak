/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.spi.resource.async;

import io.liveoak.spi.MediaType;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.state.ResourceState;

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
     * Retreive the content length produced by readContent()
     *
     * @return Content length in bytes
     */
    long contentLength();

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
    default void updateContent(RequestContext ctx, ResourceState state, Responder responder) throws Exception {
        responder.updateNotSupported(this);
    }

    /**
     * Preliminary call to determine if incoming body should be processed or not.
     * If resource determines that request is invalid it should respond with error and return false
     * which means body request processing is finished, and resource is not interested in any body that
     * may be part of the request.
     *
     * If resource will process the body, then it probably shouldn't yet write anything to responder, and just return true.
     * When body is uploaded in its entirety #updateContent method will be invoked within the same context as current willProcessUpdate().
     *
     * @param ctx
     * @param state
     * @param responder
     * @return
     * @throws Exception
     */
    default boolean willProcessUpdate(RequestContext ctx, ResourceState state, Responder responder) throws Exception {
        responder.updateNotSupported(this);
        return false;
    }
}
