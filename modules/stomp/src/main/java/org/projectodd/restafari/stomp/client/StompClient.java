package org.projectodd.restafari.stomp.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.projectodd.restafari.stomp.Headers;
import org.projectodd.restafari.stomp.Stomp;
import org.projectodd.restafari.stomp.StompException;
import org.projectodd.restafari.stomp.StompMessage;
import org.projectodd.restafari.stomp.client.protocol.ClientContext;
import org.projectodd.restafari.stomp.client.protocol.ConnectionNegotiatingHandler;
import org.projectodd.restafari.stomp.client.protocol.DisconnectionNegotiatingHandler;
import org.projectodd.restafari.stomp.client.protocol.MessageHandler;
import org.projectodd.restafari.stomp.common.*;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/**
 * STOMP client.
 *
 * <p>This client may be used in synchronous or asynchronous environments</p>
 *
 * @author Bob McWhirter
 */
public class StompClient {

    public enum ConnectionState {
        DISCONNECTED,
        CONNECTING,
        CONNECTED,
        DISCONNECTING;
    }

    /**
     * Construct a new client.
     */
    public StompClient() {

    }

    private Bootstrap createBootstrap(String host, Consumer<StompClient> callback) {
        Executor executor = Executors.newCachedThreadPool();
        NioEventLoopGroup group = new NioEventLoopGroup();

        ClientContext clientContext = new ContextImpl();

        Bootstrap bootstrap = new Bootstrap();
        bootstrap.channel(NioSocketChannel.class);
        bootstrap.group(group);
        bootstrap.handler(new ChannelInitializer<NioSocketChannel>() {
            @Override
            protected void initChannel(NioSocketChannel ch) throws Exception {
                ch.pipeline().addLast(new StompFrameEncoder());
                ch.pipeline().addLast(new StompFrameDecoder());
                ch.pipeline().addLast(new ConnectionNegotiatingHandler(clientContext, callback));
                ch.pipeline().addLast(new StompMessageEncoder(false));
                ch.pipeline().addLast(new StompMessageDecoder());
                ch.pipeline().addLast(new MessageHandler(clientContext, executor));
            }
        });

        return bootstrap;
    }

    /**
     * Retrieve the current state of the connection.
     *
     * @return The current state.
     */
    public ConnectionState getConnectionState() {
        return this.connectionState;
    }


    /**
     * Connect synchronously.
     *
     * @param host Host to connect to.
     * @param port Port to connect to.
     * @throws InterruptedException If the connection times out.
     * @throws StompException       If an error occurs during connection.
     */
    public void connectSync(String host, int port) throws InterruptedException, StompException {
        CountDownLatch latch = new CountDownLatch(1);
        connect(host, port, (client) -> {
            latch.countDown();
        });
        latch.await(30, TimeUnit.SECONDS);
    }

    /**
     * Connect asynchronously
     *
     * @param host     Host to connect to.
     * @param port     Port to connect to.
     * @param callback Callback to fire after successfully connecting.
     */
    public void connect(String host, int port, Consumer<StompClient> callback) {
        this.host = host;
        Bootstrap bootstrap = createBootstrap(host, callback);
        bootstrap.connect(host, port);
    }

    /**
     * Disconnect synchronously
     *
     * @throws InterruptedException If the disconnect times out.
     * @throws StompException       If an error occurs during disconnection.
     */
    public void disconnectSync() throws StompException, InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        disconnect(() -> {
            latch.countDown();
        });
        latch.await(30, TimeUnit.SECONDS);
    }

    /**
     * Disconnect asynchronously
     *
     * @param callback Callback to fire after successfully disconnecting.
     */
    public void disconnect(Runnable callback) {
        this.channel.pipeline().get(DisconnectionNegotiatingHandler.class).setCallback(callback);
        this.channel.writeAndFlush(StompFrame.newDisconnectFrame());
    }

    /**
     * Send a message to the server.
     *
     * <p>The message should be fully-formed, including a destination
     * header indicating where the message should be sent.</p>
     *
     * @param message The message to send.
     */
    public void send(StompMessage message) {
        this.channel.writeAndFlush(message);
    }

    /**
     * Send a message to the server.
     *
     * @param destination The destination.
     * @param content The content bytes.
     */
    public void send(String destination, ByteBuf content) {
        StompMessage message = new DefaultStompMessage();
        message.setDestination(destination);
        message.setContent(content);
        send( message );
    }

    /**
     * Send a message to the server.
     *
     * @param destination The destination.
     * @param content The content, as a UTF-8 string.
     */
    public void send(String destination, String content) {
        StompMessage message = new DefaultStompMessage();
        message.setDestination(destination);
        message.setContentAsString(content);
        send( message );
    }

    /**
     * Send a message to the server.
     *
     * @param destination The destination.
     * @param headers Additional headers.
     * @param content The content, as a UTF-8 string.
     */
    public void send(String destination, Headers headers, String content) {
        StompMessage message = new DefaultStompMessage();
        message.getHeaders().putAll( headers );
        message.setDestination(destination);
        message.setContentAsString(content);
        send( message );
    }

    /**
     * Send a message to the server.
     *
     * @param destination The destination.
     * @param headers Additional headers.
     * @param content The content bytes.
     */
    public void send(String destination, Headers headers, ByteBuf content) {
        StompMessage message = new DefaultStompMessage();
        message.getHeaders().putAll( headers );
        message.setDestination(destination);
        message.setContent(content);
        send( message );
    }

    /**
     * Subscribe to a destination.
     *
     * @param destination The destination to subscribe to.
     * @param handler     Handler for inbound messages sent from the server.
     */
    public void subscribe(String destination, Consumer<StompMessage> handler) {
        subscribe(destination, new HeadersImpl(), handler);
    }

    /**
     * Subscribe to a destination.
     *
     * <p>Additional headers may be included to support complex subscriptions.
     * The {@code destination} paramter will be added to the headers on
     * your behalf.</p>
     *
     * @param destination The destination to subscribe to.
     * @param headers     Additional headers.
     * @param handler     Handler for inbound messages sent from the server.
     */
    public void subscribe(String destination, Headers headers, Consumer<StompMessage> handler) {
        String subscriptionId = "sub-" + subscriptionCounter.getAndIncrement();
        this.subscriptions.put(subscriptionId, handler);
        StompControlFrame frame = new StompControlFrame(Stomp.Command.SUBSCRIBE);
        frame.getHeaders().putAll(headers);
        frame.setHeader( Headers.ID, subscriptionId );
        frame.setHeader(Headers.DESTINATION, destination);
        this.channel.writeAndFlush(frame);
    }

    private String host;
    private ConnectionState connectionState;
    private Stomp.Version version = Stomp.Version.VERSION_1_2;
    private Channel channel;
    private AtomicInteger subscriptionCounter = new AtomicInteger();
    private Map<String, Consumer<StompMessage>> subscriptions = new HashMap<>();

    class ContextImpl implements ClientContext {

        public StompClient getClient() {
            return StompClient.this;
        }

        public String getHost() {
            return StompClient.this.host;
        }

        public void setChannel(Channel channel) {
            StompClient.this.channel = channel;
        }

        public Channel getChannel() {
            return StompClient.this.channel;
        }

        public void setConnectionState(ConnectionState connectionState) {
            StompClient.this.connectionState = connectionState;
        }

        public ConnectionState getConnectionState() {
            return StompClient.this.connectionState;
        }

        public void setVersion(Stomp.Version version) {
            StompClient.this.version = version;
        }

        public Stomp.Version getVersion() {
            return StompClient.this.version;
        }

        public Consumer<StompMessage> getSubscriptionHandler(String subscriptionId) {
            return StompClient.this.subscriptions.get(subscriptionId);
        }

    }
}
