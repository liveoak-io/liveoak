package org.projectodd.restafari.container.codec;

import io.netty.buffer.ByteBuf;
import org.projectodd.restafari.spi.Resource;
import org.projectodd.restafari.spi.state.ResourceState;

import java.util.HashMap;
import java.util.Map;

public class ResourceCodecManager {

    public void registerResourceCodec(String mimeType, ResourceCodec codec) {
        this.codecs.put(mimeType, codec);
    }

    public ResourceState decode(String mimeType, ByteBuf buf) throws Exception {
        ResourceCodec codec = this.codecs.get(mimeType);
        return codec.decode(buf);
    }

    public ByteBuf encode(String mimeType, Resource resource) throws Exception {
        ResourceCodec codec = this.codecs.get(mimeType);
        return codec.encode(resource);
    }

    public ResourceCodec getResourceCodec(String mimeType) {
        return this.codecs.get( mimeType );
    }

    private Map<String, ResourceCodec> codecs = new HashMap<>();

}
