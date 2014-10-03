/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.common.codec;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.liveoak.spi.state.ResourceState;

/**
 * @author Bob McWhirter
 */
public class DefaultResourceState implements ResourceState {

    public DefaultResourceState() {

    }

    public DefaultResourceState(String id) {
        this.id = id;
    }

    public DefaultResourceState(ResourceState state) {
        this.id = state.id();
        this.uri = state.uri();
        for (String name: state.getPropertyNames()) {
            properties.put(name, state.getProperty(name));
        }
        for (ResourceState member: state.members()) {
            members.add(member);
        }
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
        return this.uri != null ? this.uri : ResourceState.super.uri();
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

    public Map<String, ?> propertyMap() {
        return Collections.unmodifiableMap(this.properties);
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DefaultResourceState that = (DefaultResourceState) o;

        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        if (!members.equals(that.members)) return false;
        if (!properties.equals(that.properties)) return false;
        if (uri != null ? !uri.equals(that.uri) : that.uri != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (uri != null ? uri.hashCode() : 0);
        result = 31 * result + properties.hashCode();
        result = 31 * result + members.hashCode();
        return result;
    }

    private String id;
    private URI uri;
    private Map<String, Object> properties = new LinkedHashMap<>();
    private List<ResourceState> members = new ArrayList<>();

}
