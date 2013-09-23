package org.projectodd.restafari.container.codec;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.util.Collection;

import org.projectodd.restafari.spi.Resource;

public class ToStringCodec implements ResourceCodec {

    @Override
    public ByteBuf encode(Resource resource) {
        ByteBuf buf = Unpooled.buffer();
        buf.writeBytes( resource.toString().getBytes() );
        return buf;
    }

    @Override
    public ByteBuf encode(Collection<Resource> resources) {
        ByteBuf buf = Unpooled.buffer();
        buf.writeBytes( resources.toString().getBytes() );
        return buf;
    }

}
