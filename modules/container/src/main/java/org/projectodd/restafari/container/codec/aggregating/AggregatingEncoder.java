package org.projectodd.restafari.container.codec.aggregating;

import io.netty.buffer.ByteBuf;
import org.projectodd.restafari.container.codec.EncodingContext;
import org.projectodd.restafari.container.codec.ExpansionControllingEncoder;
import org.projectodd.restafari.container.codec.ResourceEncoder;
import org.projectodd.restafari.spi.MediaType;
import org.projectodd.restafari.spi.resource.Resource;
import org.projectodd.restafari.spi.resource.async.BinaryContentSink;
import org.projectodd.restafari.spi.resource.async.BinaryResource;
import org.projectodd.restafari.spi.resource.async.CollectionResource;

/**
 * @author Bob McWhirter
 */
public class AggregatingEncoder implements ExpansionControllingEncoder<ByteBuf> {

    public AggregatingEncoder(MediaType mediaType) {
        this.mediaType = mediaType;
    }

    @Override
    public ByteBuf createAttachment(ByteBuf output) throws Exception {
        return output;
    }

    @Override
    public boolean shouldEncodeContent(EncodingContext<ByteBuf> object) {
        return true;
    }

    @Override
    public void encode(EncodingContext<ByteBuf> context) throws Exception {
        Object object = context.object();

        if (object instanceof BinaryResource) {
            if (((BinaryResource) object).mediaType().isCompatible(this.mediaType)) {
                ((BinaryResource) object).readContent(new MyBinaryContentSink(context));
            } else {
                context.end();
            }
        } else if (object instanceof CollectionResource) {
            context.encodeContent(() -> {
                context.end();
            });
        } else {
            context.end();
        }
    }

    @Override
    public void close(EncodingContext<ByteBuf> context) throws Exception {
    }

    private MediaType mediaType;

    private static class MyBinaryContentSink implements BinaryContentSink {

        private EncodingContext<ByteBuf> context;

        public MyBinaryContentSink(EncodingContext<ByteBuf> context) {
            this.context = context;
        }

        @Override
        public void close() {
            this.context.end();
        }

        @Override
        public void accept(ByteBuf byteBuf) {
            this.context.attachment().writeBytes(byteBuf);
        }
    }

}
