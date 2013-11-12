package org.projectodd.restafari.container.codec;

import org.projectodd.restafari.spi.state.ResourceState;

import java.util.*;

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
    public void putProperty(String name, Object value) {
        this.properties.put(name, value);
    }

    @Override
    public Object getProperty(String name) {
        return this.properties.get( name );
    }

    @Override
    public Set<String> getPropertyNames() {
        return this.properties.keySet();
    }

    @Override
    public void addMember(ResourceState member) {
        this.members.add( member );
    }

    @Override
    public List<ResourceState> members() {
        return this.members;
    }

    public String toString() {
        return "[DefaultResourceState: id=" + this.id + "; properties=" + this.properties + "; members=" + this.members + "]";
    }

    private String id;
    private Map<String, Object> properties = new HashMap<>();
    private List<ResourceState> members = new ArrayList<>();

}
