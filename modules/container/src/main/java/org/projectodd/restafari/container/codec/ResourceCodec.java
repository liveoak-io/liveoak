package org.projectodd.restafari.container.codec;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.projectodd.restafari.spi.resource.Resource;
import org.projectodd.restafari.spi.state.ResourceState;

import java.util.concurrent.CompletableFuture;

/**
 * @author Bob McWhirter
 */
public class ResourceCodec {

    public ResourceCodec(ResourceEncoder encoder, ResourceDecoder decoder) {
        this.encoder = encoder;
        this.decoder = decoder;
    }

    public ByteBuf encode(Resource resource) throws Exception {
        CompletableFuture<ByteBuf> future = new CompletableFuture<>();
        newEncodingContext(resource, future).encode();
        ByteBuf result = future.get();
        return result;
    }

    public ResourceState decode(ByteBuf resource) throws Exception {
        return this.decoder.decode(resource);
    }

    protected EncodingContext newEncodingContext(Resource resource, CompletableFuture<ByteBuf> future) throws Exception {
        ByteBuf buffer = Unpooled.buffer();
        Object attachment = this.encoder.createAttachment(buffer);
        EncodingContext driver = new RootEncodingContext(this.encoder, attachment, resource, () -> {
            future.complete( buffer );
        });
        return driver;
    }


    private final ResourceEncoder encoder;
    private final ResourceDecoder decoder;

}
