package io.liveoak.stomp.client;

import java.util.UUID;
import java.util.function.Consumer;

import io.liveoak.stomp.Headers;
import io.liveoak.stomp.StompMessage;

/**
 * @author Bob McWhirter
 */
public class SubscriptionImpl implements Subscription, ReceiptReceiver {


    public SubscriptionImpl(String destination, Headers headers) {
        this.destination = destination;
        this.headers = headers;
        this.subscriptionId = UUID.randomUUID().toString();
    }

    public SubscriptionImpl onMessage(Consumer<StompMessage> onMessage) {
        this.onMessage = onMessage;
        return this;
    }

    public SubscriptionImpl onReceipt(Runnable onReceipt) {
        this.onReceipt = onReceipt;
        return this;
    }

    public Consumer<StompMessage> onMessage() {
        return this.onMessage;
    }

    public Runnable onReceipt() {
        return this.onReceipt;
    }

    public String destination() {
        return this.destination;
    }

    public Headers headers() {
        return this.headers;
    }

    public String subscriptionId() {
        return this.subscriptionId;
    }

    @Override
    public void receivedReceipt() {
        if ( this.onReceipt != null ) {
            this.onReceipt.run();
        }
    }

    public String receiptId() {
        return this.subscriptionId;
    }

    private String subscriptionId;
    private final String destination;
    private final Headers headers;

    private Consumer<StompMessage> onMessage;
    private Runnable onReceipt;

}
