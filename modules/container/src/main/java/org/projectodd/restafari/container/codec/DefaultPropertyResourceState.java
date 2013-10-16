package org.projectodd.restafari.container.codec;

import org.projectodd.restafari.spi.state.PropertyResourceState;

/**
 * @author Bob McWhirter
 */
public class DefaultPropertyResourceState implements PropertyResourceState {

    public DefaultPropertyResourceState(Object value) {
        this.value = value;
    }

    public DefaultPropertyResourceState(String id, Object value) {
        this.id = id;
        this.value = value;
    }

    @Override
    public String id() {
        return this.id;
    }

    public void id(String id) {
        this.id = id;
    }

    @Override
    public Object value() {
        return this.value;
    }

    public void value(Object value) {
        this.value = value;
    }

    public String toString() {
        return "[" + this.id + " = " + this.value + "]";

    }

    private String id;
    private Object value;
}


