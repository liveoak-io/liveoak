package org.projectodd.restafari.container.codec.state;

import io.netty.buffer.ByteBuf;
import org.projectodd.restafari.container.codec.DefaultCollectionResourceState;
import org.projectodd.restafari.container.codec.DefaultObjectResourceState;
import org.projectodd.restafari.container.codec.EncodingContext;
import org.projectodd.restafari.container.codec.ResourceEncoder;
import org.projectodd.restafari.spi.resource.Resource;
import org.projectodd.restafari.spi.resource.async.BinaryResource;
import org.projectodd.restafari.spi.resource.async.CollectionResource;
import org.projectodd.restafari.spi.resource.async.ObjectResource;
import org.projectodd.restafari.spi.resource.async.PropertyResource;
import org.projectodd.restafari.spi.state.CollectionResourceState;
import org.projectodd.restafari.spi.state.ObjectResourceState;
import org.projectodd.restafari.spi.state.ResourceState;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 * @author Bob McWhirter
 */
public class ResourceStateEncoder implements ResourceEncoder<ResourceStateEncoder.EncoderState> {

    public static class EncoderState {
        private ResourceState root;
        private Stack<ResourceState> stack = new Stack<>();

        void push(ResourceState state) {
            this.stack.push(state);

            if (this.stack.size() == 1) {
                this.root = state;
            }
        }

        ResourceState pop() {
            ResourceState popped = this.stack.pop();
            if (!this.stack.isEmpty()) {
                ResourceState top = this.stack.peek();
                if (top instanceof DefaultCollectionResourceState) {
                    ((DefaultCollectionResourceState) top).addResource(popped);
                }
            }
            return popped;
        }

        ResourceState peek() {
            return this.stack.peek();
        }

        public ResourceState root() {
            return this.root;
        }
    }

    @Override
    public EncoderState createAttachment(ByteBuf output) throws Exception {
        return new EncoderState();
    }

    @Override
    public void encode(EncodingContext<EncoderState> context) throws Exception {
        Object object = context.object();
        System.err.println(context.depth() + " // " + object);

        if (object instanceof CollectionResource) {
            encodeCollection(context);
        } else if (object instanceof ObjectResource) {
            encodeObject(context);
        } else if (object instanceof PropertyResource) {
            encodeProperty(context);
        } else if (object instanceof BinaryResource) {
            encodeBinary(context);
        } else {
            encodeValue(context);
        }
    }

    protected void encodeCollection(EncodingContext<EncoderState> context) throws Exception {
        Resource resource = (Resource) context.object();
        EncoderState state = context.attachment();

        state.push(new DefaultCollectionResourceState(resource.id()));

        context.encodeContent(() -> {
            state.pop();
            context.end();
        });
    }

    protected void encodeObject(EncodingContext<EncoderState> context) throws Exception {
        Resource resource = (Resource) context.object();
        EncoderState state = context.attachment();

        state.push(new DefaultObjectResourceState(resource.id()));

        context.encodeContent(() -> {
            state.pop();
            context.end();
        });
    }

    protected void encodeProperty(EncodingContext<EncoderState> context) throws Exception {
        PropertyResource resource = (PropertyResource) context.object();
        EncoderState state = context.attachment();

        ResourceState top = state.peek();
        if (top instanceof ObjectResourceState) {
            ((ObjectResourceState) top).addProperty(resource.id(), resource.get(null));
            context.end();
        } else if (top instanceof CollectionResourceState) {
            // TODO allow properties on Collections?
        }

    }

    protected void encodeBinary(EncodingContext<EncoderState> context) throws Exception {

    }

    protected void encodeValue(EncodingContext<EncoderState> context) throws Exception {

    }

    @Override
    public void close(EncodingContext<EncoderState> context) throws Exception {
        //To change body of implemented methods use File | Settings | File Templates.
    }


}
