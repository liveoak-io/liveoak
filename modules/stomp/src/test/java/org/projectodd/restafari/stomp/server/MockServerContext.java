package org.projectodd.restafari.stomp.server;

import org.projectodd.restafari.stomp.Headers;
import org.projectodd.restafari.stomp.StompMessage;

import java.util.*;

/**
 * @author Bob McWhirter
 */
public class MockServerContext implements ServerContext {

    @Override
    public void handleConnect(StompConnection connection) {
    }

    @Override
    public void handleDisconnect(StompConnection connection) {
        this.subscriptions.removeIf((e) -> {
            return (e.connection.equals(connection));
        });
    }

    @Override
    public void handleSubscribe(StompConnection connection, String destination, String subscriptionId, Headers header) {
        this.subscriptions.add(new Subscription(connection, destination, subscriptionId));
    }

    @Override
    public void handleUnsubscribe(StompConnection connection, String subscriptionId) {
        this.subscriptions.removeIf((e) -> {
            return (e.connection.equals(connection) && e.subscriptionId.equals(subscriptionId));
        });
    }

    @Override
    public void handleSend(StompConnection connection, StompMessage message) {
        StompMessage retainedDupe = message.duplicate().retain();
        this.sentMessages.add(retainedDupe);
        String destination = message.headers().get(Headers.DESTINATION);
        this.subscriptions.forEach((e) -> {
            if (e.destination.equals(destination)) {
                StompMessage dupe = message.duplicate().retain();
                dupe.headers().put( Headers.SUBSCRIPTION, e.subscriptionId );
                e.connection.send(dupe);
            }
        });
    }

    public List<StompMessage> getSentMessages() {
        return this.sentMessages;
    }

    private static class Subscription {
        public StompConnection connection;
        public String destination;
        public String subscriptionId;

        public Subscription(StompConnection connection, String destination, String subscriptionId) {
            this.connection = connection;
            this.destination = destination;
            this.subscriptionId = subscriptionId;
        }
    }

    private List<StompMessage> sentMessages = new ArrayList<>();
    private List<Subscription> subscriptions = new ArrayList<>();
}
