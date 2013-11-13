package io.liveoak.vertx.modules.resource;

import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

/**
 * @author Bob McWhirter
 */
public class VertxResponder {

    public VertxResponder(Message<JsonObject> request) {
        this.request = request;
    }

    public void resourceRead(JsonObject resource) {
        JsonObject response = ResponseBuilder.newReadResponse( resource );
        this.request.reply( response );
    }

    // TODO Really, this should probably be more link ResourceSink instead
    // which provides .accept() [or something vertx-named] and .end()
    // to allow the resource to asynchronously provide them.
    // and maybe the mBaaS end looks for a series of messages
    // to accumulate as the response.  Maybe.
    // I don't think a vertx message can have N replies, though, so we
    // probably need to setup some temporary address and communicate that
    // through the envelope.
    public void resourcesRead(JsonArray resources) {
        JsonObject response = ResponseBuilder.newReadResponse( resources );
        this.request.reply( response );
    }

    public void noSuchResource(String id) {
        JsonObject response = ResponseBuilder.newNoSuchResourceResponse( id );
        this.request.reply( response );
    }

    private Message<JsonObject> request;
}
