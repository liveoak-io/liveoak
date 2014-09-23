/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */

package io.liveoak.testtools.resources;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import io.liveoak.common.codec.DefaultResourceState;
import io.liveoak.common.util.ResourceConversionUtils;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.resource.SynchronousResource;
import io.liveoak.spi.resource.async.Resource;
import io.liveoak.spi.resource.async.Responder;
import io.liveoak.spi.state.ResourceState;

/**
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class MockInMemoryResource implements SynchronousResource {

    protected Resource parent;
    private String id;
    private Map<String, String> properties = new LinkedHashMap<>();
    private Map<String, MockInMemoryResource> members = new LinkedHashMap<>();

    public MockInMemoryResource(String id) {
        this.id = id;
    }

    @Override
    public Resource parent() {
        return parent;
    }

    public void parent(Resource parent) {
        this.parent = parent;
    }

    public MockInMemoryResource parentAsMock() {
        return (MockInMemoryResource)parent;
    }

    @Override
    public String id() {
        return id;
    }

    @Override
    public Collection<? extends Resource> members() {
        return members.values();
    }

    @Override
    public Resource member(String id) {
        return members.get(id);
    }

    @Override
    public ResourceState properties() throws Exception {
        ResourceState state = new DefaultResourceState(id);
        for (String id : properties.keySet()) {
            state.putProperty(id, properties.get(id));
        }
        state.uri(uri());
        return state;
    }

    @Override
    public void properties(ResourceState props) throws Exception {
        properties.clear();
        for (String key : props.getPropertyNames()) {
            putProperty(key, (String)props.getProperty(key));
        }
    }

    @Override
    public void createMember(RequestContext ctx, ResourceState child, Responder responder) throws Exception {
        MockInMemoryResource createdChild = addMember(child.id());
        for (String key : child.getPropertyNames()) {
            createdChild.putProperty(key, (String)child.getProperty(key));
        }

        Resource resource = ResourceConversionUtils.convertResourceState(child, this);
        responder.resourceCreated(resource);
    }

    @Override
    public void delete(RequestContext ctx, Responder responder) throws Exception {
        if (parent instanceof MockInMemoryResource) {
            parentAsMock().members.remove(this.id());
            responder.resourceDeleted(this);
        } else {
            responder.deleteNotSupported(this);
        }
    }

    public MockInMemoryResource putProperty(String key, String value) {
        properties.put(key, value);
        return this;
    }

    public MockInMemoryResource addMember(String key) {
        MockInMemoryResource child = new MockInMemoryResource(key);
        child.parent = this;
        members.put(key, child);
        return child;
    }

}
