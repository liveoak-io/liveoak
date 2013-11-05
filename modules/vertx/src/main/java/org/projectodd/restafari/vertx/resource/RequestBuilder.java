package org.projectodd.restafari.vertx.resource;

import org.projectodd.restafari.spi.Pagination;
import org.vertx.java.core.json.JsonObject;

/**
 * @author Bob McWhirter
 */
public class RequestBuilder {

    public static JsonObject newCreateRequest(JsonObject state) {
        JsonObject message = newBaseRequest("create");
        message.putObject( "state", state );
        return message;
    }

    public static JsonObject newReadRequest(String id) {
        JsonObject message = newBaseRequest("read");
        message.putString( "id", id );
        return message;
    }

    public static JsonObject newReadRequest(String id, Pagination pagination) {
        JsonObject message = newBaseRequest("read");
        message.putString( "id", id );
        message.putNumber( "offset", pagination.getOffset() );
        message.putNumber( "limit", pagination.getLimit() );
        return message;
    }

    public static JsonObject newUpdateRequest(String id, JsonObject state) {
        JsonObject message = newBaseRequest( "update" );
        message.putString( "id", id );
        message.putObject( "state", state );
        return message;
    }

    public static JsonObject newDeleteRequest(String id) {
        JsonObject message = newBaseRequest( "delete" );
        message.putString( "id", id );
        return message;
    }

    protected static JsonObject newBaseRequest(String action) {
        JsonObject message = new JsonObject();
        message.putString( "action", action );
        return message;
    }

}
