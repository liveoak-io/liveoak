package org.projectodd.restafari.container.codec;

import io.netty.buffer.ByteBuf;
import org.projectodd.restafari.spi.Resource;
import org.projectodd.restafari.spi.state.ResourceState;

/**
 * @author Bob McWhirter
 */
public class ResourceCodec {

    public ResourceCodec(ResourceEncoder encoder, ResourceDecoder decoder) {
        this.encoder = encoder;
        this.decoder = decoder;
    }

    public ByteBuf encode(Resource resource) throws Exception {
        return newEncodingDriver().encode( resource );
    }

    public ResourceState decode(ByteBuf resource) throws Exception {
        return this.decoder.decode( resource );
    }

    protected EncodingDriver newEncodingDriver() throws Exception {
        EncodingDriver driver = new EncodingDriver( this.encoder );
        return driver;
    }


    private final ResourceEncoder encoder;
    private final ResourceDecoder decoder;

}
