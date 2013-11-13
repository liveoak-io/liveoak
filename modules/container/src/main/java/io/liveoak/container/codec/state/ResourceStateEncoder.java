package io.liveoak.container.codec.state;

import io.netty.buffer.ByteBuf;
import io.liveoak.container.codec.DefaultResourceState;
import io.liveoak.container.codec.Encoder;
import io.liveoak.spi.resource.async.Resource;
import io.liveoak.spi.state.ResourceState;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Stack;

/**
 * @author Bob McWhirter
 */
public class ResourceStateEncoder implements Encoder {

    private Stack<Object> stack = new Stack<>();
    private ResourceState root;

    public ResourceStateEncoder() {

    }

    public ResourceState root() {
        return this.root;
    }

    @Override
    public void initialize(ByteBuf buffer) throws Exception {
        // nothing
    }

    @Override
    public void close() throws Exception {

    }

    @Override
    public void startResource(Resource resource) throws Exception {
        ResourceState state = new DefaultResourceState(resource.id());
        this.stack.push( state );

        if ( this.root == null ) {
            root = state;
        }
    }

    @Override
    public void endResource(Resource resource) throws Exception {
        Object completed = this.stack.pop();
        if ( ! this.stack.isEmpty() ) {
            Object top = this.stack.peek();
            if ( top instanceof Collection ) {
                ((Collection)top).add( completed );
            } else if ( top instanceof ResourceState ) {
                ((ResourceState)top).addMember((ResourceState) completed);
            } else if ( top instanceof PropertyCatcher ) {
                ((PropertyCatcher)top).value = completed;
            }
        }
    }

    @Override
    public void startProperty(String propertyName) throws Exception {
        this.stack.push( new PropertyCatcher() );
    }

    @Override
    public void endProperty(String propertyName) throws Exception {
        PropertyCatcher catcher = (PropertyCatcher) this.stack.pop();
        ((ResourceState) this.stack.peek()).putProperty(propertyName, catcher.value);
    }

    @Override
    public void startMembers() throws Exception {
    }

    @Override
    public void endMembers() throws Exception {
    }

    @Override
    public void startList() throws Exception {
        this.stack.push( new ArrayList<Object>() );
    }

    @Override
    public void endList() throws Exception {
        ArrayList<Object> completed = (ArrayList<Object>) this.stack.pop();

        if ( ! this.stack.isEmpty() ) {
            Object top = this.stack.peek();
            if ( top instanceof Collection ) {
                ((Collection) top).add( completed );
            } else if ( top instanceof PropertyCatcher ) {
                ((PropertyCatcher) top).value = completed;
            }
        }
    }

    @Override
    public void writeValue(String value) throws Exception {
        Object top = this.stack.peek();

        if ( top instanceof Collection ) {
            ((Collection) top).add( value );
        } else if ( top instanceof PropertyCatcher ) {
            ((PropertyCatcher) top).value = value;
        }
    }

    @Override
    public void writeValue(Integer value) throws Exception {
        Object top = this.stack.peek();

        if ( top instanceof Collection ) {
            ((Collection) top).add( value );
        } else if ( top instanceof PropertyCatcher ) {
            ((PropertyCatcher) top).value = value;
        }
    }

    @Override
    public void writeValue(Double value) throws Exception {
        Object top = this.stack.peek();

        if ( top instanceof Collection ) {
            ((Collection) top).add( value );
        } else if ( top instanceof PropertyCatcher ) {
            ((PropertyCatcher) top).value = value;
        }
    }

    @Override
    public void writeValue(Date value) throws Exception {
        Object top = this.stack.peek();

        if ( top instanceof Collection ) {
            ((Collection) top).add( value );
        } else if ( top instanceof PropertyCatcher ) {
            ((PropertyCatcher) top).value = value;
        }
    }

    private static class PropertyCatcher {
        public Object value;
    }
}
