package org.projectodd.restafari.container.codec;

import org.projectodd.restafari.spi.state.ObjectResourceState;
import org.projectodd.restafari.spi.state.PropertyResourceState;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * @author Bob McWhirter
 */
public class DefaultObjectResourceState implements ObjectResourceState {

    public DefaultObjectResourceState() {

    }

    public DefaultObjectResourceState(String id) {
        this.id = id;

    }

    @Override
    public String id() {
        return id;
    }

    public void id(String id) {
        this.id = id;
    }

    public void addProperty(String name, Object value) {
        addProperty(new DefaultPropertyResourceState(name, value));
    }

    public void addProperty(PropertyResourceState property) {
        if (property.id() != null && property.id().equals("id")) {
            this.id = property.value().toString();
        }
        this.properties.add(property);
    }

    public Object getProperty(String name) {
        Optional<PropertyResourceState> found = this.properties.stream().filter((e) -> {
            return name.equals(e.id());
        }).findFirst();

        if ( found.isPresent() ) {
            return found.get().value();
        }

        return null;
    }

    @Override
    public Stream<? extends PropertyResourceState> members() {
        return this.properties.stream();
    }

    public String toString() {
        return "[Object: id=" + this.id + "; properties=" + this.properties + "]";
    }

    private String id;
    private List<PropertyResourceState> properties = new ArrayList<>();
}
