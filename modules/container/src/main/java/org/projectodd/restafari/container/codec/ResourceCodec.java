package org.projectodd.restafari.container.codec;

import io.netty.buffer.ByteBuf;

import java.io.IOException;
import java.util.Collection;

import org.projectodd.restafari.spi.Resource;

public interface ResourceCodec {
    
    ByteBuf encodeResource(Resource resource) throws IOException;
    ByteBuf encodeResources(Collection<Resource> resources) throws IOException;

    Resource decodeResource(ByteBuf resource);
    Collection<Resource> decodeResources(ByteBuf resources);


}
