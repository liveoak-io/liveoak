package io.liveoak.vertx.modules.resource;

import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

/**
 * @author Bob McWhirter
 */
public class ResponseBuilder {

    public static JsonObject newCreatedResponse(JsonObject state) {
        JsonObject message = newBaseResponse( 200, "Resource created" );
        message.putString( "id", state.getString( "id" ) );
        message.putObject( "content", state );
        return message;
    }

    public static JsonObject newReadResponse(JsonObject state) {
        JsonObject message = newBaseResponse( 200, "OK" );
        message.putString( "id", state.getString( "id" ) );
        message.putString( "type", "object" );
        message.putObject( "content", state );
        return message;
    }

    public static JsonObject newReadResponse(JsonObject[] state) {
        JsonObject message = newBaseResponse( 200, "OK" );
        message.putString( "type", "collection" );
        message.putArray( "content", new JsonArray( state ) );
        return message;
    }
    public static JsonObject newReadResponse(JsonArray state) {
        JsonObject message = newBaseResponse( 200, "OK" );
        message.putString( "type", "collection" );
        message.putArray( "content", state );
        return message;
    }

    public static JsonObject newUpdatedResponse(JsonObject state) {
        JsonObject message = newBaseResponse( 200, "Resource updated" );
        message.putString( "id", state.getString( "id" ) );
        message.putObject( "content", state );
        return message;
    }

    public static JsonObject newDeletedResponse(JsonObject state) {
        JsonObject message = newBaseResponse( 200, "Resource deleted" );
        message.putString( "id", state.getString( "id" ) );
        message.putObject( "content", state );
        return message;
    }

    public static JsonObject newNoSuchResourceResponse(String id) {
        JsonObject message = newBaseResponse( 404, "No such resource: " + id );
        return message;
    }

    public static JsonObject newCreateNotSupportedResponse(String id) {
        return newNotSupportedResponse( "Create", id );
    }

    public static JsonObject newReadNotSupportedResponse(String id) {
        return newNotSupportedResponse( "Read", id );
    }

    public static JsonObject newUpdateNotSupportedResponse(String id) {
        return newNotSupportedResponse( "Update", id );
    }

    public static JsonObject newDeleteNotSupportedResponse(String id) {
        return newNotSupportedResponse( "Delete", id );
    }

    protected static JsonObject newNotSupportedResponse(String action, String id) {
        return newBaseResponse( 405, action + " not supported" );
    }

    protected static JsonObject newBaseResponse(int statusCode, String statusMessage) {
        JsonObject message = new JsonObject();
        message.putNumber( "status-code", statusCode );
        message.putString( "status-message", statusMessage);
        return message;
    }
}
