package org.projectodd.restafari.spi.resource.async;

import org.projectodd.restafari.spi.resource.Resource;

/**
 * @author Bob McWhirter
 */
public class SimplePropertyResource implements PropertyResource {

    public SimplePropertyResource(ObjectResource parent, String name, Object value) {
        this.parent = parent;
        this.name = name;
        this.value = value;
    }

    @Override
    public void set(Object value) {
        this.value = value;
    }

    @Override
    public Object get() {
        return value;
    }

    @Override
    public Resource parent() {
        return parent;
    }

    @Override
    public String id() {
        return this.name;
    }

    @Override
    public void read(String id, Responder responder) {
        responder.readNotSupported( this );
    }

    @Override
    public void delete(Responder responder) {
        responder.deleteNotSupported( this );
    }

    private ObjectResource parent;
    private String name;
    private Object value;
}
