/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.container.subscriptions;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import io.liveoak.common.codec.ResourceCodec;
import io.liveoak.common.codec.ResourceCodecManager;
import io.liveoak.spi.MediaType;
import io.liveoak.spi.container.SubscriptionManager;
import io.liveoak.stomp.Headers;
import io.liveoak.stomp.StompMessage;
import io.liveoak.stomp.server.StompConnection;
import io.liveoak.stomp.server.StompServerContext;

/**
 * @author Bob McWhirter
 * @author Ken Finnigan
 */
public class ContainerStompServerContext implements StompServerContext {

    public ContainerStompServerContext(ResourceCodecManager codecManager, SubscriptionManager subscriptionManager) {
        this.codecManager = codecManager;
        this.subscriptionManager = subscriptionManager;
    }

    @Override
    public void handleConnect(StompConnection connection, String applicationId) {
    }

    @Override
    public void handleDisconnect(StompConnection connection) {
        List<String> subscriptionIds = this.subscriptionIdsByConnection.remove(connection.getConnectionId());
        if (subscriptionIds != null && subscriptionIds.size() > 0) {
            subscriptionIds.forEach(id -> this.subscriptionManager.removeSubscriptionById(StompSubscription.generateId(connection, id)));
        }
    }

    @Override
    public void handleSubscribe(StompConnection connection, String destination, String subscriptionId, Headers headers) {
        String acceptMediaType = headers.get("accept");
        if (acceptMediaType == null) {
            acceptMediaType = "application/json";
        }
        MediaType mediaType = this.codecManager.determineMediaType(acceptMediaType, null);
        if (mediaType == null) {
            mediaType = MediaType.JSON;
        }

        ResourceCodec codec = this.codecManager.getResourceCodec(mediaType);
        StompSubscription subscription = new StompSubscription(connection, destination, subscriptionId, mediaType, codec);
        this.subscriptionManager.addSubscription(subscription);
        subscriptionIdsForConnection(connection.getConnectionId()).add(subscriptionId);
    }

    @Override
    public void handleUnsubscribe(StompConnection connection, String subscriptionId) {
        this.subscriptionManager.removeSubscriptionById(StompSubscription.generateId(connection, subscriptionId));
        subscriptionIdsForConnection(connection.getConnectionId()).remove(subscriptionId);
    }

    @Override
    public void handleSend(StompConnection connection, StompMessage message) {
    }

    private List<String> subscriptionIdsForConnection(String connectionId) {
        return this.subscriptionIdsByConnection.computeIfAbsent(connectionId, (key) -> new ArrayList<>());
    }

    private ResourceCodecManager codecManager;
    protected SubscriptionManager subscriptionManager;
    protected Map<String, List<String>> subscriptionIdsByConnection = new ConcurrentHashMap<>();
}
