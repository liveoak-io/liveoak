package org.projectodd.restafari.vertx.resource;

import org.projectodd.restafari.spi.RequestContext;
import org.projectodd.restafari.spi.resource.Resource;
import org.projectodd.restafari.spi.resource.async.CollectionResource;
import org.projectodd.restafari.spi.resource.async.ResourceSink;
import org.projectodd.restafari.spi.resource.async.Responder;
import org.projectodd.restafari.spi.state.ResourceState;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonObject;

/**
 * @author Bob McWhirter
 */
public class VertxCollectionResource extends AbstractVertxResource implements CollectionResource {

    public VertxCollectionResource(String id, String address) {
        this(null, id, null, address );
    }

    public VertxCollectionResource(Resource parent, String id, Vertx vertx, String address) {
        super(parent, id, vertx );
        this.address = address;
    }

    public String address() {
        return this.address;
    }

    @Override
    public void create(RequestContext ctx, ResourceState state, Responder responder) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void readContent(RequestContext ctx, ResourceSink sink) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void read(RequestContext ctx, String id, Responder responder) {
        JsonObject request = new JsonObject();
        request.putString("action", "read");
        request.putString("id", id);
        vertx().eventBus().send(address(), request, (Message<JsonObject> response) -> {
            JsonObject payload = response.body();
            System.err.println( "payload: " + payload );
            int status = payload.getInteger("status");
            if (status == 200) {
                String type = payload.getString("type");
                if (type.equals("collection")) {
                    String resourceAddress = payload.getString("address");
                    responder.resourceRead(new VertxCollectionResource(this, id, vertx(), resourceAddress));
                } else if (type.equals("object")) {
                    responder.resourceRead(new VertxObjectResource(this, id, vertx(), payload.getObject( "state" ) ) );
                }
            } else if (status == 404) {
                responder.noSuchResource(id);
            }
        });
    }

    private String address;
}
