package org.projectodd.restafari.container.codec;

import io.netty.buffer.ByteBuf;

import java.io.IOException;

import org.projectodd.restafari.spi.*;
import org.projectodd.restafari.spi.state.ResourceState;

public interface ResourceEncoder<T> {
    void encode(Resource resource, EncodingDriver<T> driver) throws IOException;
    T createEncodingContext();
}
