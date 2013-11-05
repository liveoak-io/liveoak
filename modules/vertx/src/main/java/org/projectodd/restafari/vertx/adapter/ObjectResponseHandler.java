package org.projectodd.restafari.vertx.adapter;

/**
 * @author Bob McWhirter
 */
public interface ObjectResponseHandler {

    public void handle(String id, VertxResponder responder);
}
