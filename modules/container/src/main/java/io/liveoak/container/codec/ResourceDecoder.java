package io.liveoak.container.codec;

import io.netty.buffer.ByteBuf;
import io.liveoak.spi.state.ResourceState;

import java.io.IOException;

public interface ResourceDecoder {
    ResourceState decode(ByteBuf resource) throws IOException;
}
