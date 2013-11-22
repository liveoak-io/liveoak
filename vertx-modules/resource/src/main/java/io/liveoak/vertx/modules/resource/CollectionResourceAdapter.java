/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.vertx.modules.resource;

import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonObject;

import java.util.UUID;

/**
 * @author Bob McWhirter
 */
public class CollectionResourceAdapter implements Handler<Message<JsonObject>> {

    public CollectionResourceAdapter(Vertx vertx, String id, String registrationAddress) {
        this.vertx = vertx;
        this.id = id;
        this.registrationAddress = registrationAddress;
    }

    public void createHandler(Handler<Message<JsonObject>> createHandler) {
        this.createHandler = createHandler;
    }

    public void readMembersHandler(CollectionResponseHandler readMembersHandler) {
        this.readMembersHandler = readMembersHandler;
    }

    public void readMemberHandler(ObjectResponseHandler readMemberHandler) {
        this.readMemberHandler = readMemberHandler;
    }

    public void deleteHandler(Handler<Message<JsonObject>> deleteHandler) {
        this.deleteHandler = deleteHandler;
    }

    public void start() {
        this.address = "resource." + UUID.randomUUID().toString();

        this.vertx.eventBus().registerHandler(this.address, this);
        this.vertx.eventBus().send(registrationAddress,
                new JsonObject()
                        .putString("action", "register")
                        .putString("address", this.address)
                        .putString("id", this.id));
    }

    public void stop() {
        this.vertx.eventBus().send(this.registrationAddress,
                new JsonObject()
                        .putString("action", "unregister")
                        .putString("id", this.id));

        this.vertx.eventBus().unregisterHandler(this.address, this);
    }

    @Override
    public void handle(Message<JsonObject> message) {
        JsonObject body = message.body();
        String action = body.getString("action");

        switch (action) {
            case "create":
                break;
            case "readMember":
                String id = body.getString("id");
                if (id != null) {
                    if (this.readMemberHandler == null) {
                        message.reply(new JsonObject().putNumber("status", 404));
                    } else {
                        VertxResponder responder = new VertxResponder(message);
                        this.readMemberHandler.handle(id, responder);
                    }
                } else {
                    if (this.readMembersHandler == null) {
                        message.reply(ResponseBuilder.newReadNotSupportedResponse("ummm?"));
                    } else {
                        VertxResponder responder = new VertxResponder(message);
                        this.readMembersHandler.handle(responder);
                    }
                }
                break;
            case "update":
            case "delete":
                break;
        }
    }


    private Vertx vertx;
    private String id;
    private String address;

    private String registrationAddress;

    private Handler<Message<JsonObject>> createHandler;
    private CollectionResponseHandler readMembersHandler;
    private ObjectResponseHandler readMemberHandler;
    private Handler<Message<JsonObject>> deleteHandler;

}


