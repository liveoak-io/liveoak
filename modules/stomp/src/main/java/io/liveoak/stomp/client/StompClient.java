/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.stomp.client;

import io.liveoak.stomp.Headers;
import io.liveoak.stomp.Stomp;
import io.liveoak.stomp.StompException;
import io.liveoak.stomp.StompMessage;
import io.liveoak.stomp.client.protocol.ConnectionNegotiatingHandler;
import io.liveoak.stomp.client.protocol.DisconnectionNegotiatingHandler;
import io.liveoak.stomp.client.protocol.MessageHandler;
import io.liveoak.stomp.client.protocol.ReceiptHandler;
import io.liveoak.stomp.client.protocol.StompClientContext;
import io.liveoak.stomp.client.protocol.SubscriptionEncoder;
import io.liveoak.stomp.common.DefaultStompMessage;
import io.liveoak.stomp.common.HeadersImpl;
import io.liveoak.stomp.common.StompControlFrame;
import io.liveoak.stomp.common.StompFrame;
import io.liveoak.stomp.common.StompFrameDecoder;
import io.liveoak.stomp.common.StompFrameEncoder;
import io.liveoak.stomp.common.StompMessageDecoder;
import io.liveoak.stomp.common.StompMessageEncoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

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

        StompClientContext clientContext = new ContextImplStomp();

        Bootstrap bootstrap = new Bootstrap();
        bootstrap.channel(NioSocketChannel.class);
        bootstrap.group(group);
        bootstrap.handler(new ChannelInitializer<NioSocketChannel>() {
            @Override
            protected void initChannel(NioSocketChannel ch) throws Exception {
                //ch.pipeline().addLast( new DebugHandler( "client-head" ) );
                ch.pipeline().addLast(new StompFrameEncoder());
                ch.pipeline().addLast(new StompFrameDecoder());
                ch.pipeline().addLast( new SubscriptionEncoder() );
                ch.pipeline().addLast( new ReceiptHandler(executor));
                //ch.pipeline().addLast( new DebugHandler( "client-frames" ) );
                ch.pipeline().addLast(new ConnectionNegotiatingHandler(clientContext, callback));
                ch.pipeline().addLast(new StompMessageEncoder(false));
                ch.pipeline().addLast(new StompMessageDecoder());
                ch.pipeline().addLast(new MessageHandler(executor));
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
     * @param content     The content bytes.
     */
    public void send(String destination, ByteBuf content) {
        StompMessage message = new DefaultStompMessage();
        message.destination(destination);
        message.content(content.duplicate().retain());
        send(message);
    }

    /**
     * Send a message to the server.
     *
     * @param destination The destination.
     * @param content     The content, as a UTF-8 string.
     */
    public void send(String destination, String content) {
        StompMessage message = new DefaultStompMessage();
        message.destination(destination);
        message.content(content);
        send(message);
    }

    /**
     * Send a message to the server.
     *
     * @param destination The destination.
     * @param headers     Additional headers.
     * @param content     The content, as a UTF-8 string.
     */
    public void send(String destination, Headers headers, String content) {
        StompMessage message = new DefaultStompMessage();
        message.headers().putAll(headers);
        message.destination(destination);
        message.content(content);
        send(message);
    }

    /**
     * Send a message to the server.
     *
     * @param destination The destination.
     * @param headers     Additional headers.
     * @param content     The content bytes.
     */
    public void send(String destination, Headers headers, ByteBuf content) {
        StompMessage message = new DefaultStompMessage();
        message.headers().putAll(headers);
        message.destination(destination);
        message.content(content);
        send(message);
    }

    /**
     * Subscribe to a destination.
     *
     * @param destination The destination to subscribe to.
     * @param subscriptionSetup Code to setup subscription
     */
    public void subscribe(String destination, Consumer<Subscription> subscriptionSetup) {
        subscribe(destination, new HeadersImpl(), subscriptionSetup);
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
     * @param subscriptionSetup Code to setup subscription
     */
    public void subscribe(String destination, Headers headers, Consumer<Subscription> subscriptionSetup) {
        SubscriptionImpl subscription = new SubscriptionImpl( destination, headers );
        subscriptionSetup.accept(subscription);
        this.channel.writeAndFlush( subscription );
    }

    private String host;
    private ConnectionState connectionState;
    private Stomp.Version version = Stomp.Version.VERSION_1_2;
    private Channel channel;
    private AtomicInteger subscriptionCounter = new AtomicInteger();
    private Map<String, Consumer<StompMessage>> subscriptions = new HashMap<>();

    class ContextImplStomp implements StompClientContext {

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
    }
}
