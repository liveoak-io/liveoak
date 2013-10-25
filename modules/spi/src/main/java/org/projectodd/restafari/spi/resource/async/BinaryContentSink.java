package org.projectodd.restafari.spi.resource.async;

import io.netty.buffer.ByteBuf;

import java.util.function.Consumer;

/**
 * @author Bob McWhirter
 */
public interface BinaryContentSink extends Consumer<ByteBuf>, AutoCloseable {
    void close();
}
