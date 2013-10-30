package org.projectodd.restafari.spi.resource.async;

import org.projectodd.restafari.spi.RequestContext;
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
    public Object get(RequestContext ctx) {
        return value;
    }

    @Override
    public void readContent(RequestContext ctx, PropertyContentSink sink) {
        sink.accept( this.value );
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
    public void read(RequestContext ctx, String id, Responder responder) {
        responder.readNotSupported( this );
    }

    @Override
    public void delete(RequestContext ctx, Responder responder) {
        responder.deleteNotSupported( this );
    }

    private ObjectResource parent;
    private String name;
    private Object value;
}
