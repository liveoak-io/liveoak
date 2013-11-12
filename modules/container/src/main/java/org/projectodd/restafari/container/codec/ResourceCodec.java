package org.projectodd.restafari.container.codec;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.projectodd.restafari.container.DefaultContainer;
import org.projectodd.restafari.container.codec.driver.EncodingDriver;
import org.projectodd.restafari.container.codec.driver.RootEncodingDriver;
import org.projectodd.restafari.spi.RequestContext;
import org.projectodd.restafari.spi.resource.async.Resource;
import org.projectodd.restafari.spi.state.ResourceState;

import java.util.concurrent.CompletableFuture;

/**
 * @author Bob McWhirter
 */
public class ResourceCodec {

    public ResourceCodec(DefaultContainer container, Class<? extends Encoder> encoderClass, ResourceDecoder decoder) {
        this.container = container;
        this.encoderClass = encoderClass;
        this.decoder = decoder;
    }

    public ByteBuf encode(RequestContext ctx, Resource resource) throws Exception {
        CompletableFuture<ByteBuf> future = new CompletableFuture<>();
        newEncodingDriver(ctx, resource, future).encode();
        ByteBuf result = future.get();
        return result;
    }

    public ResourceState decode(ByteBuf resource) throws Exception {
        return this.decoder.decode(resource);
    }

    protected EncodingDriver newEncodingDriver(RequestContext ctx, Resource resource, CompletableFuture<ByteBuf> future) throws Exception {
        ByteBuf buffer = Unpooled.buffer();
        Encoder encoder = this.encoderClass.newInstance();
        encoder.initialize( buffer );
        RootEncodingDriver driver = new RootEncodingDriver( ctx, encoder, resource, ()->{ future.complete(buffer); } );
        return driver;
    }


    private final DefaultContainer container;
    private final Class<? extends Encoder> encoderClass;
    private final ResourceDecoder decoder;

}
