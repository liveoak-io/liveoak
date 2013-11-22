/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.vertx.resource;

import io.liveoak.spi.RequestContext;
import io.liveoak.spi.resource.async.Resource;
import io.liveoak.spi.resource.async.ResourceSink;
import io.liveoak.spi.resource.async.Responder;
import io.liveoak.spi.state.ResourceState;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

import java.util.Iterator;

/**
 * @author Bob McWhirter
 */
public class VertxCollectionResource extends AbstractVertxResource implements Resource {

    public VertxCollectionResource(String id, String address) {
        this(null, id, null, address);
    }

    public VertxCollectionResource(Resource parent, String id, Vertx vertx, String address) {
        super(parent, id, vertx);
        this.address = address;
    }

    public String address() {
        return this.address;
    }

    @Override
    public void createMember(RequestContext ctx, ResourceState state, Responder responder) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void readMembers(RequestContext ctx, ResourceSink sink) {
        JsonObject request = new JsonObject();
        request.putString("action", "readMember");
        // TODO: Allow filtering, paging and depth
        vertx().eventBus().send(address(), request, (Message<JsonObject> response) -> {
            JsonObject payload = response.body();
            int status = payload.getInteger("status-code");
            if (status == 200) {
                String type = payload.getString("type");
                if (type.equals("collection")) {
                    System.err.println("GOT: " + payload);
                    JsonArray records = payload.getArray("content");
                    Iterator<Object> iterator = records.iterator();
                    while (iterator.hasNext()) {
                        JsonObject record = (JsonObject) iterator.next();
                        sink.accept(new VertxObjectResource(this, record.getString("id"), vertx(), record));
                    }
                } else {
                    // TODO: I don't think we ever get here
                }
            }
            sink.close();
        });
    }

    @Override
    public void readMember(RequestContext ctx, String id, Responder responder) {
        JsonObject request = RequestBuilder.newReadRequest(id);
        vertx().eventBus().send(address(), request, (Message<JsonObject> response) -> {
            JsonObject payload = response.body();
            System.err.println("payload: " + payload);
            int status = payload.getInteger("status-code");
            if (status == 200) {
                String type = payload.getString("type");
                // TODO I don't think this ever evals true
                if (type.equals("collection")) {
                    String resourceAddress = payload.getString("address");
                    responder.resourceRead(new VertxCollectionResource(this, id, vertx(), resourceAddress));
                } else if (type.equals("object")) {
                    responder.resourceRead(new VertxObjectResource(this, id, vertx(), payload.getObject("content")));
                }
            } else if (status == 404) {
                responder.noSuchResource(id);
            }
        });
    }

    private String address;
}
