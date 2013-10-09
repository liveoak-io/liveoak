package org.projectodd.restafari.stomp.client;

import org.projectodd.restafari.stomp.StompMessage;

/**
 * @author Bob McWhirter
 */
public interface SubscriptionHandler {

    void onMessage(StompMessage message);

}
