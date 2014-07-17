package io.liveoak.stomp.client;

import java.util.function.Consumer;

import io.liveoak.stomp.StompMessage;

/**
 * @author Bob McWhirter
 */
public interface Subscription {

    Subscription onMessage(Consumer<StompMessage> onMessage);

    Subscription onReceipt(Runnable onReceipt);
}
