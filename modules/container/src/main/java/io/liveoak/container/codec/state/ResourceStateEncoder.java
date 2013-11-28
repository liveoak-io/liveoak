/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.container.codec.state;

import io.liveoak.container.codec.DefaultResourceState;
import io.liveoak.container.codec.Encoder;
import io.liveoak.spi.resource.async.Resource;
import io.liveoak.spi.state.ResourceState;
import io.netty.buffer.ByteBuf;

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
        if (resource.id() != null) {
            if (resource.uri() == null) {
                throw new IllegalStateException("Resource has id() but not uri(): " + resource.id());
            }
            state.uri(resource.uri());
        }
        this.stack.push(state);

        if (this.root == null) {
            root = state;
        }
    }

    @Override
    public void endResource(Resource resource) throws Exception {
        Object completed = this.stack.pop();
        if (!this.stack.isEmpty()) {
            Object top = this.stack.peek();
            if (top instanceof Collection) {
                ((Collection) top).add(completed);
            } else if (top instanceof ResourceState) {
                ((ResourceState) top).addMember((ResourceState) completed);
            } else if (top instanceof PropertyCatcher) {
                ((PropertyCatcher) top).value = completed;
            }
        }
    }

    @Override
    public void startProperties() throws Exception {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void endProperties() throws Exception {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void startProperty(String propertyName) throws Exception {
        this.stack.push(new PropertyCatcher());
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
        this.stack.push(new ArrayList<Object>());
    }

    @Override
    public void endList() throws Exception {
        ArrayList<Object> completed = (ArrayList<Object>) this.stack.pop();

        if (!this.stack.isEmpty()) {
            Object top = this.stack.peek();
            if (top instanceof Collection) {
                ((Collection) top).add(completed);
            } else if (top instanceof PropertyCatcher) {
                ((PropertyCatcher) top).value = completed;
            }
        }
    }

    @Override
    public void writeValue(String value) throws Exception {
        write(value);
    }

    @Override
    public void writeValue(Integer value) throws Exception {
        write(value);
    }

    @Override
    public void writeValue(Double value) throws Exception {
        write(value);
    }

    @Override
    public void writeValue( Long value ) throws Exception {
        write(value);
    }

    @Override
    public void writeValue( Boolean value ) throws Exception {
        write(value);
    }

    @Override
    public void writeValue(Date value) throws Exception {
        write(value);
    }

    @Override
    public void writeLink(Resource resource) throws Exception {
        Object top = this.stack.peek();

        if (top instanceof Collection) {
            ((Collection) top).add(resource.uri());
        } else if (top instanceof ResourceState) {
            DefaultResourceState state = new DefaultResourceState();
            state.id(resource.id());
            state.uri(resource.uri());
            ((ResourceState) top).addMember(state);
        } else if (top instanceof PropertyCatcher) {
            ((PropertyCatcher) top).value = resource.uri();
        }
    }

    private static class PropertyCatcher {
        public Object value;
    }

    private void write(Object value) throws Exception{
        Object top = this.stack.peek();

        if (top instanceof Collection) {
            ((Collection) top).add(value);
        } else if (top instanceof PropertyCatcher) {
            ((PropertyCatcher) top).value = value;
        }
    }
}
