package org.projectodd.restafari.container.codec;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.projectodd.restafari.spi.Resource;
import org.projectodd.restafari.spi.ResourceSink;

import java.io.IOException;

/**
 * @author Bob McWhirter
 */
public class EncodingDriver<T> implements ResourceSink {

    public EncodingDriver(ResourceEncoder<T> encoder) throws Exception {
        this.buffer = Unpooled.buffer();
        this.encoder = encoder;
        this.context = encoder.createEncodingContext();
    }

    public T encodingContext() {
        return this.context;
    }

    ByteBuf encode(Resource resource) throws IOException {
        System.err.println( "encode: " + resource + " // " + this.depth );
        ++this.depth;
        this.encoder.encode( resource, this );
        --this.depth;
        return this.buffer();
    }

    public ByteBuf buffer() {
        return this.buffer;
    }

    @Override
    public void accept(Resource resource) {
        if ( shouldExpand( resource ) ) {
            try {
                ++this.depth;
                System.err.println( "accept: " + resource + " // " + this.depth );
                this.encoder.encode( resource, this );
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                --this.depth;
            }
        }
    }

    @Override
    public void close() {
    }

    public boolean shouldExpand(Resource resource) {
        return this.depth <= 1;
    }

    private int depth;
    private ResourceEncoder encoder;
    private ByteBuf buffer;
    private T context;

}
