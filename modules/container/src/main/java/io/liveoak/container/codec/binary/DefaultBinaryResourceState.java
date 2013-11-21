/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.container.codec.binary;

import io.liveoak.container.codec.DefaultResourceState;
import io.liveoak.spi.MediaType;
import io.liveoak.spi.state.BinaryResourceState;
import io.netty.buffer.ByteBuf;

/**
 * @author <a href="http://community.jboss.org/people/kenfinni">Ken Finnigan</a>
 */
public class DefaultBinaryResourceState extends DefaultResourceState implements BinaryResourceState {

    ByteBuf buffer;

    public DefaultBinaryResourceState() {
    }

    public DefaultBinaryResourceState(ByteBuf buffer) {
        this.buffer = buffer;
    }

    @Override
    public String getMimeType() {
        return MediaType.OCTET_STREAM.toString();
    }

    @Override
    public ByteBuf getBuffer() {
        return buffer;
    }
}
