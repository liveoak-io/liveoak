package org.projectodd.restafari.container.codec;

import io.netty.buffer.ByteBuf;

import java.util.Collection;

import org.projectodd.restafari.spi.Resource;

public interface ResourceCodec {
    
    ByteBuf encode(Resource resource);
    ByteBuf encode(Collection<Resource> resources);

}
