/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */

package io.liveoak.security.impl.interceptor;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import io.liveoak.common.codec.DefaultResourceState;
import io.liveoak.spi.resource.SynchronousResource;
import io.liveoak.spi.resource.async.Resource;
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
        ResourceState state = new DefaultResourceState();
        for (String id : properties.keySet()) {
            state.putProperty(id, properties.get(id));
        }
        return state;
    }

    public MockInMemoryResource addProperty(String key, String value) {
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
