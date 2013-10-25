package org.projectodd.restafari.container.codec;

import io.netty.buffer.ByteBuf;
import org.projectodd.restafari.spi.MediaType;

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
