package org.projectodd.restafari.container.codec;

import io.netty.buffer.ByteBuf;
import org.projectodd.restafari.spi.Resource;
import org.projectodd.restafari.spi.state.ResourceState;

import java.io.IOException;

public interface ResourceDecoder {
    ResourceState decode(ByteBuf resource) throws IOException;
}
