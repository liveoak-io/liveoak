package io.liveoak.stomp.client;

import io.liveoak.stomp.Headers;

/**
 * @author Ken Finnigan
 */
public class Unsubscribe {
    public Unsubscribe(String subscriptionId, Headers headers) {
        this.subscriptionId = subscriptionId;
        this.headers = headers;
    }

    public Headers headers() {
        return this.headers;
    }

    public String subscriptionId() {
        return this.subscriptionId;
    }

    private final String subscriptionId;
    private final Headers headers;
}
