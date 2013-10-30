package org.projectodd.restafari.container;

import org.projectodd.restafari.spi.RequestContext;
import org.projectodd.restafari.spi.resource.Resource;
import org.projectodd.restafari.spi.resource.async.ObjectResource;
import org.projectodd.restafari.spi.resource.async.ResourceSink;
import org.projectodd.restafari.spi.resource.async.Responder;
import org.projectodd.restafari.spi.resource.async.SimplePropertyResource;
import org.projectodd.restafari.spi.state.ObjectResourceState;
import org.projectodd.restafari.spi.state.PropertyResourceState;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Bob McWhirter
 */
public class InMemoryObjectResource implements ObjectResource {

    public InMemoryObjectResource(InMemoryCollectionResource parent, String id, ObjectResourceState state) {
        this.parent = parent;
        this.id = id;
        this.state = state;
    }

    public Resource parent() {
        return this.parent;
    }

    public String id() {
        return this.id;
    }

    @Override
    public void read(RequestContext ctx, String id, Responder responder) {
        boolean found = false;

        List<PropertyResourceState> result = state.members().collect(Collectors.toList());

        for ( PropertyResourceState each : result ) {
            if ( each.id().equals( id ) ) {
                responder.resourceRead( new SimplePropertyResource( this, each.id(), each.value() ) );
                return;
            }
        }

        responder.noSuchResource( id );
    }

    @Override
    public void delete(RequestContext ctx, Responder responder) {
        this.parent.delete(this.id);
        responder.resourceDeleted(this);
    }

    @Override
    public void update(RequestContext ctx, ObjectResourceState state, Responder responder) {
        this.state = state;
        responder.resourceUpdated(this);
    }

    @Override
    public void readContent(RequestContext ctx, ResourceSink sink) {
        this.state.members().forEach((prop) -> {
            sink.accept( new SimplePropertyResource(this, prop.id(), prop.value() ) );
        });
        try {
            sink.close();
        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    public String toString() {
        return "[InMemoryObjectResource: id=" + this.id + "]";
    }

    private InMemoryCollectionResource parent;
    private String id;
    private ObjectResourceState state;

}
