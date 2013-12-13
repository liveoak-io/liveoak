/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.common.codec;

import io.liveoak.spi.state.ResourceState;
import io.netty.buffer.ByteBuf;

import java.io.IOException;

public interface ResourceDecoder {
    ResourceState decode(ByteBuf resource) throws IOException;
}
