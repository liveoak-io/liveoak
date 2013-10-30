package org.projectodd.restafari.vertx.resource;

import org.projectodd.restafari.spi.RequestContext;
import org.projectodd.restafari.spi.resource.Resource;
import org.projectodd.restafari.spi.resource.async.ObjectResource;
import org.projectodd.restafari.spi.resource.async.ResourceSink;
import org.projectodd.restafari.spi.resource.async.Responder;
import org.projectodd.restafari.spi.resource.async.SimplePropertyResource;
import org.projectodd.restafari.spi.state.ObjectResourceState;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.json.JsonObject;

/**
 * @author Bob McWhirter
 */
public class VertxObjectResource extends AbstractVertxResource implements ObjectResource {

    public VertxObjectResource(Resource parent, String id, Vertx vertx, JsonObject state) {
        super( parent, id, vertx );
        this.state = state;
    }

    @Override
    public void update(RequestContext ctx, ObjectResourceState state, Responder responder) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void readContent(RequestContext ctx, ResourceSink sink) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void read(RequestContext ctx, String id, Responder responder) {
        Object value = this.state.getValue( id );
        if ( value == null ) {
            responder.noSuchResource( id );
        } else {
            responder.resourceRead( new SimplePropertyResource( this, id, value ));
        }
    }

    private JsonObject state;
}
