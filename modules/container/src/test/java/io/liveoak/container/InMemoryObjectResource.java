/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.container;

import io.liveoak.spi.RequestContext;
import io.liveoak.spi.resource.BlockingResource;
import io.liveoak.spi.resource.async.PropertySink;
import io.liveoak.spi.resource.async.Resource;
import io.liveoak.spi.resource.async.Responder;
import io.liveoak.spi.state.ResourceState;

/**
 * @author Bob McWhirter
 */
public class InMemoryObjectResource implements Resource, BlockingResource {

    public InMemoryObjectResource(InMemoryCollectionResource parent, String id, ResourceState state) {
        this.parent = parent;
        this.id = id;
        this.state = state;
    }

    public Resource parent() {
        return this.parent;
    }

    public String id() {
        return this.id;
    }

    public ResourceState state() {
        return state;
    }

    @Override
    public void delete(RequestContext ctx, Responder responder) {
        this.parent.delete(this.id);
        responder.resourceDeleted(this);
    }

    @Override
    public void readProperties(RequestContext ctx, PropertySink sink) throws Exception {
        for (String name : this.state.getPropertyNames()) {
            sink.accept(name, this.state.getProperty(name));
        }
        sink.close();
    }

    @Override
    public void updateProperties(RequestContext ctx, ResourceState state, Responder responder) {
        this.state = state;
        responder.resourceUpdated(this);
    }

    public String toString() {
        return "[InMemoryObjectResource: id=" + this.id + "; state=" + this.state + "]";
    }

    private InMemoryCollectionResource parent;
    private String id;
    private ResourceState state;

}
