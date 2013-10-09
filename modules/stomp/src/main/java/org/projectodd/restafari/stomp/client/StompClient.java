package org.projectodd.restafari.stomp.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.projectodd.restafari.stomp.Headers;
import org.projectodd.restafari.stomp.Stomp;
import org.projectodd.restafari.stomp.StompException;
import org.projectodd.restafari.stomp.StompMessage;
import org.projectodd.restafari.stomp.client.protocol.ConnectionNegotiatingHandler;
import org.projectodd.restafari.stomp.client.protocol.MessageHandler;
import org.projectodd.restafari.stomp.common.*;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * @author Bob McWhirter
 */
public class StompClient {


    public StompClient() {

    }

    private Bootstrap createBootstrap(String host) {
        this.clientContext = new ClientContext(host);

        Executor executor = Executors.newCachedThreadPool();
        NioEventLoopGroup group = new NioEventLoopGroup();

        Bootstrap bootstrap = new Bootstrap();
        bootstrap.channel(NioSocketChannel.class);
        bootstrap.group(group);
        bootstrap.handler(new ChannelInitializer<NioSocketChannel>() {
            @Override
            protected void initChannel(NioSocketChannel ch) throws Exception {
                ch.pipeline().addLast(new DebugHandler("client-head"));
                ch.pipeline().addLast(new StompFrameEncoder());
                ch.pipeline().addLast(new StompFrameDecoder());
                ch.pipeline().addLast(new ConnectionNegotiatingHandler(StompClient.this.clientContext));
                ch.pipeline().addLast(new MessageHandler(StompClient.this.clientContext, executor));
                ch.pipeline().addLast(new DebugHandler("client-tail"));
            }
        });

        return bootstrap;
    }


    public void connectSync(String host, int port) throws InterruptedException, StompException {
        Bootstrap bootstrap = createBootstrap( host );
        ChannelFuture connectFuture = bootstrap.connect(host, port);
        connectFuture.sync();
        this.clientContext.waitForConnect();
        this.channel = connectFuture.channel();
    }

    public void connect(String host, int port, Consumer<StompClient> callback) throws InterruptedException {
        Bootstrap bootstrap = createBootstrap( host );
        ChannelFuture connectFuture = bootstrap.connect(host, port);
        connectFuture.addListener((f) -> {
            this.channel = connectFuture.channel();
            callback.accept(this);
        });
    }

    public void disconnectSync() throws StompException, InterruptedException {
        this.channel.write(StompFrame.newDisconnectFrame());
        this.channel.flush();
        this.clientContext.waitForDisconnect();
    }

    public void send(StompMessage message) {
        this.channel.write(message);
        this.channel.flush();
    }

    public void subscribe(String destination, SubscriptionHandler handler) {
        subscribe(destination, new HeadersImpl(), handler);
    }

    public void subscribe(String destination, Headers headers, SubscriptionHandler handler) {
        String subscriptionId = "sub-" + subscriptionCounter.getAndIncrement();
        this.clientContext.addSubscription(subscriptionId, handler);
        StompControlFrame frame = new StompControlFrame(Stomp.Command.SUBSCRIBE);
        frame.getHeaders().putAll(headers);
        frame.setHeader(Headers.DESTINATION, destination);
        this.channel.write(frame);
        this.channel.flush();
    }

    private ClientContext clientContext;
    private Channel channel;
    private AtomicInteger subscriptionCounter = new AtomicInteger();
}
