package org.projectodd.restafari.spi;

import io.netty.buffer.ByteBuf;

import java.util.Collection;


public interface ObjectResourceCodec {
    
    ByteBuf encodeResource(ObjectResource resource);
    ObjectResource decodeResource(ByteBuf buf);
    
    ByteBuf encodeResources(Collection<ObjectResource> resources);
    Collection<ObjectResource> decodeResources(ByteBuf buf);

}
