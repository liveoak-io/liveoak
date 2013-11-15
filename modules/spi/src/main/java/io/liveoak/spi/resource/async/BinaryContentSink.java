/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.spi.resource.async;

import io.netty.buffer.ByteBuf;

import java.util.function.Consumer;

/**
 * @author Bob McWhirter
 */
public interface BinaryContentSink extends Consumer<ByteBuf>, AutoCloseable {
    void close();
}
