/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.container.codec;

import io.liveoak.spi.state.ResourceState;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Bob McWhirter
 */
public class DefaultResourceState implements ResourceState {

    public DefaultResourceState() {

    }

    public DefaultResourceState(String id) {
        this.id = id;
    }

    @Override
    public String id() {
        return this.id;
    }

    @Override
    public void id(String id) {
        this.id = id;
    }

    @Override
    public void uri(URI uri) {
        this.uri = uri;
    }

    @Override
    public URI uri() {
        return this.uri;
    }

    @Override
    public void putProperty(String name, Object value) {
        this.properties.put(name, value);
    }

    @Override
    public Object getProperty(String name) {
        return this.properties.get(name);
    }

    @Override
    public Object removeProperty(String name) {
        return this.properties.remove(name);
    }

    @Override
    public Set<String> getPropertyNames() {
        return this.properties.keySet();
    }

    @Override
    public void addMember(ResourceState member) {
        this.members.add(member);
    }

    @Override
    public List<ResourceState> members() {
        return this.members;
    }

    public String toString() {
        return "[DefaultResourceState: id=" + this.id + "; uri=" + this.uri() + "; properties=" + this.properties + "; members=" + this.members + "]";
    }

    private String id;
    private URI uri;
    private Map<String, Object> properties = new LinkedHashMap<>();
    private List<ResourceState> members = new ArrayList<>();

}
