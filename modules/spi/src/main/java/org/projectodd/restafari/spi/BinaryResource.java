package org.projectodd.restafari.spi;

import io.netty.buffer.ByteBuf;

public interface BinaryResource extends Resource {
    
    String getMimeType();

}
