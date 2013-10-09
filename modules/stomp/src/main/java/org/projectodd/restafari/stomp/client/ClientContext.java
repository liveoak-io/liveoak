package org.projectodd.restafari.stomp.client;

import org.projectodd.restafari.stomp.Stomp;
import org.projectodd.restafari.stomp.StompException;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * @author Bob McWhirter
 */
public class ClientContext {


    public enum State {
        DISCONNECTED,
        CONNECTING,
        CONNECTED,
        DISCONNECTING;
    }

    public ClientContext(String host) {
        this.host = host;
        this.connectionState = State.DISCONNECTED;
    }

    public String getHost() {
        return this.host;
    }

    public void setVersion(Stomp.Version version) {
        this.version = version;
    }

    public Stomp.Version getVersion() {
        return this.version;
    }

    public void setConnectionState(State connectionState) {
        this.connectionState = connectionState;
        if ( this.connectionState == State.CONNECTING || this.connectionState == State.DISCONNECTING ) {
            this.latch = new CountDownLatch(1);
        } else if ( this.connectionState == State.CONNECTED || this.connectionState == State.DISCONNECTED ) {
            this.latch.countDown();
        }
    }

    public State getConnectionState() {
        return connectionState;
    }

    void addSubscription(String subscriptionId, SubscriptionHandler handler) {
        this.subscriptions.put( subscriptionId, handler );
    }

    public SubscriptionHandler getSubscriptionHandler(String subscriptionId) {
        return this.subscriptions.get( subscriptionId );
    }

    void waitForConnect() throws InterruptedException, StompException {
        this.latch.await( 30, TimeUnit.SECONDS );
        if ( this.connectionState != State.CONNECTED ) {
            throw new StompException( "Not connected" );
        }
    }

    void waitForDisconnect() throws InterruptedException, StompException {
        this.latch.await( 30, TimeUnit.SECONDS );
        if ( this.connectionState != State.DISCONNECTED ) {
            throw new StompException( "Not disconnected" );
        }
    }

    private String host;
    private Stomp.Version version;
    private State connectionState;
    private Map<String, SubscriptionHandler> subscriptions = new HashMap<>();
    private CountDownLatch latch;
}
