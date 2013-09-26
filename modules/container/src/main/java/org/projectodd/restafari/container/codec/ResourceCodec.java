package org.projectodd.restafari.container.codec;

import io.netty.buffer.ByteBuf;

import java.io.IOException;
import java.util.Collection;

import org.projectodd.restafari.spi.Resource;

public interface ResourceCodec {
    
    ByteBuf encode(Resource resource) throws IOException;
    ByteBuf encode(Collection<Resource> resources) throws IOException;

    Object decode(ByteBuf resource) throws IOException;

}
