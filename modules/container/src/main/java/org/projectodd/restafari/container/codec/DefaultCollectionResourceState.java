package org.projectodd.restafari.container.codec;

import org.projectodd.restafari.spi.state.CollectionResourceState;
import org.projectodd.restafari.spi.state.ResourceState;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * @author Bob McWhirter
 */
public class DefaultCollectionResourceState implements CollectionResourceState {

    public DefaultCollectionResourceState() {

    }

    public DefaultCollectionResourceState(String id) {
        this.id = id;
    }

    @Override
    public String id() {
        return this.id;
    }

    public void id(String id) {
        this.id = id;
    }

    public void addResource(ResourceState state) {
        this.resources.add( state );
    }

    @Override
    public Stream<? extends ResourceState> members() {
        return resources.stream();
    }

    public String toString() {
        return "[Collection: id=" + this.id + "; members=" + this.resources + "]";
    }

    private String id;
    private List<ResourceState> resources = new ArrayList<>();

}
