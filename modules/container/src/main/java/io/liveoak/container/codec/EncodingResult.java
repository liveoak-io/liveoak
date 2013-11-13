package io.liveoak.container.codec;

import io.netty.buffer.ByteBuf;
import io.liveoak.spi.MediaType;

/**
 * @author Bob McWhirter
 */
public class EncodingResult {

    public EncodingResult(MediaType mediaType, ByteBuf encoded) {
        this.mediaType = mediaType;
        this.encoded = encoded;
    }

    public MediaType mediaType() {
        return this.mediaType;
    }

    public ByteBuf encoded() {
        return this.encoded;
    }

    private MediaType mediaType;
    private ByteBuf encoded;
}
