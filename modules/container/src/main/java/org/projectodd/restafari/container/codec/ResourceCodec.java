package org.projectodd.restafari.container.codec;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.projectodd.restafari.container.DefaultContainer;
import org.projectodd.restafari.spi.RequestContext;
import org.projectodd.restafari.spi.resource.Resource;
import org.projectodd.restafari.spi.state.ResourceState;

import java.util.concurrent.CompletableFuture;

/**
 * @author Bob McWhirter
 */
public class ResourceCodec {

    public ResourceCodec(DefaultContainer container, ResourceEncoder encoder, ResourceDecoder decoder) {
        this.container = container;
        this.encoder = encoder;
        this.decoder = decoder;
    }

    public ByteBuf encode(RequestContext ctx, Resource resource) throws Exception {
        CompletableFuture<ByteBuf> future = new CompletableFuture<>();
        newEncodingContext(ctx, resource, future).encode();
        ByteBuf result = future.get();
        return result;
    }

    public ResourceState decode(ByteBuf resource) throws Exception {
        return this.decoder.decode(resource);
    }

    protected EncodingContext newEncodingContext(RequestContext ctx, Resource resource, CompletableFuture<ByteBuf> future) throws Exception {
        ByteBuf buffer = Unpooled.buffer();
        Object attachment = this.encoder.createAttachment(buffer);
        EncodingContext driver = new RootEncodingContext(ctx, this.encoder, attachment, resource, container.resourceAspectManager(), () -> {
            future.complete( buffer );
        });
        return driver;
    }


    private final DefaultContainer container;
    private final ResourceEncoder encoder;
    private final ResourceDecoder decoder;

}
