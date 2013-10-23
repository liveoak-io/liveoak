package org.projectodd.restafari.vertx.adapter;

import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonObject;

/**
 * @author Bob McWhirter
 */
public class CollectionResourceAdapter implements Handler<Message<JsonObject>> {

    public CollectionResourceAdapter(Vertx vertx, String address) {
        this.vertx = vertx;
        this.address = address;
    }

    public void createHandler(Handler<Message<JsonObject>> createHandler) {
        this.createHandler = createHandler;
    }

    public void readMembersHandler(Handler<Message<JsonObject>> readContentHandler) {
        this.readMembersHandler = readMembersHandler;
    }

    public void readMemberHandler(Handler<Message<JsonObject>> readMemberHandler) {
        this.readMemberHandler = readMemberHandler;
    }

    public void deleteHandler(Handler<Message<JsonObject>> deleteHandler) {
        this.deleteHandler = deleteHandler;
    }

    public void start() {
        this.vertx.eventBus().registerHandler(this.address, this );
    }

    public void stop() {
        this.vertx.eventBus().unregisterHandler( this.address, this );
    }

    @Override
    public void handle(Message<JsonObject> message) {
        JsonObject body = (JsonObject) message.body();
        String action = body.getString( "action" );

        switch ( action ) {
            case "create":
                break;
            case "read":
                String id = body.getString( "id" );
                if ( id != null ) {
                    if ( this.readMemberHandler == null ) {
                        message.reply( new JsonObject().putNumber( "status", 404 ));
                    } else {
                        this.readMemberHandler.handle( message );
                    }
                } else {
                    if ( this.readMembersHandler == null ) {
                        message.reply( new JsonObject().putNumber( "status", 404 ) );
                    } else {
                        this.readMembersHandler.handle( message );
                    }
                }
                break;
            case "delete":
                break;
        }
    }



    private Vertx vertx;
    private String address;

    private Handler<Message<JsonObject>> createHandler;
    private Handler<Message<JsonObject>> readMembersHandler;
    private Handler<Message<JsonObject>> readMemberHandler;
    private Handler<Message<JsonObject>> deleteHandler;

}


