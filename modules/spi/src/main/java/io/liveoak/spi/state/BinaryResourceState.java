/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.spi.state;

import io.netty.buffer.ByteBuf;

/**
 * @author Bob McWhirter
 * @author <a href="http://community.jboss.org/people/kenfinni">Ken Finnigan</a>
 */
public interface BinaryResourceState extends ResourceState {

    /**
     * Retrieve the Mime Type of the binary resource.
     *
     * @return The Mime Type of the binary resource.
     */
    String getMimeType();

    /**
     * Retrieve the buffer containing the binary resource content.
     *
     * @return The {@link ByteBuf} containing the binary resource content.
     */
    ByteBuf getBuffer();

}
