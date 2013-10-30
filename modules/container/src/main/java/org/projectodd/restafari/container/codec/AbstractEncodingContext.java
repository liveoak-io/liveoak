package org.projectodd.restafari.container.codec;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.projectodd.restafari.spi.RequestContext;
import org.projectodd.restafari.spi.resource.Resource;
import org.projectodd.restafari.spi.resource.async.*;

import java.util.LinkedList;

/**
 * @author Bob McWhirter
 */
public class AbstractEncodingContext<T> implements EncodingContext<T> {

    public AbstractEncodingContext(AbstractEncodingContext<T> parent, Object object, Runnable completionHandler) {
        this.parent = parent;
        this.object = object;
        this.completionHandler = completionHandler;
    }

    public int depth() {
        if ( this.parent == null ) {
            return 0;
        }
        return this.parent.depth() + 1;
    }

    public T attachment() {
        if (this.parent != null) {
            return this.parent.attachment();
        }
        return null;
    }

    public AbstractEncodingContext parent() {
        return this.parent;
    }

    public ResourceEncoder encoder() {
        if (this.parent != null) {
            return this.parent.encoder();
        }
        return null;
    }

    public void encode() throws Exception {
        encoder().encode(this);
    }

    public Object object() {
        return this.object;
    }

    public void end() {
        this.completionHandler.run();
    }

    public boolean shouldEncodeContent() {
        if ( encoder() instanceof ExpansionControllingEncoder ) {
            return ((ExpansionControllingEncoder) encoder()).shouldEncodeContent( this );
        }


        if ( this.object instanceof PropertyResource ) {
            return true;
        }

        if ( this.parent != null && this.parent.object instanceof CollectionResource ) {
            return depth() < 2;
        }

        return depth() < 1;
    }

    public void encodeContent(Runnable endContentHandler) {
        if (shouldEncodeContent()) {
            this.endContentHandler = endContentHandler;

            if (this.object instanceof CollectionResource) {
                ((CollectionResource) this.object).readContent(RequestContext.instance().getPagination(), new MyCollectionContentSink());
            } else if (this.object instanceof ObjectResource) {
                ((ObjectResource) this.object).readContent(new MyObjectContentSink());
            } else if (this.object instanceof PropertyResource) {
                ((PropertyResource) this.object).readContent(new MyPropertyContentSink());
            } else if (this.object instanceof BinaryResource) {
                ((BinaryResource) this.object).readContent(new MyBinaryContentSink());
            }
        } else {
            endContentHandler.run();
        }
    }

    protected void encodeNextContent() {
        if (children.isEmpty()) {
            endContentHandler.run();
        } else {
            EncodingContext next = children.removeFirst();
            try {
                next.encode();
            } catch (Exception e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }
    }

    private AbstractEncodingContext<T> parent;
    private Object object;
    private Runnable endContentHandler;
    private Runnable completionHandler;
    private LinkedList<EncodingContext> children = new LinkedList<>();

    private class MyCollectionContentSink implements ResourceSink {

        @Override
        public void accept(Resource resource) {
            ChildEncodingContext child = new ChildEncodingContext(AbstractEncodingContext.this, resource);
            children.add(child);
        }

        @Override
        public void close() {
            encodeNextContent();
        }

    }

    private class MyObjectContentSink implements ResourceSink {
        @Override
        public void accept(Resource resource) {
            ChildEncodingContext child = new ChildEncodingContext(AbstractEncodingContext.this, resource);
            children.add(child);
        }

        @Override
        public void close() {
            encodeNextContent();
        }
    }

    private class MyPropertyContentSink implements PropertyContentSink {

        @Override
        public void accept(Object o) {
            ChildEncodingContext child = new ChildEncodingContext(AbstractEncodingContext.this, o);
            children.add(child);
            encodeNextContent();
        }
    }

    private class MyBinaryContentSink implements BinaryContentSink {

        private ByteBuf buffer = Unpooled.buffer();

        @Override
        public void accept(ByteBuf byteBuf) {
            buffer.writeBytes( byteBuf );
        }

        @Override
        public void close() {
            ChildEncodingContext child = new ChildEncodingContext(AbstractEncodingContext.this, buffer);
            children.add( child );
            encodeNextContent();
        }

    }


}
