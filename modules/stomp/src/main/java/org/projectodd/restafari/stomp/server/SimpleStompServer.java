package org.projectodd.restafari.stomp.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.concurrent.Future;
import org.projectodd.restafari.stomp.common.*;
import org.projectodd.restafari.stomp.server.protocol.*;

/**
 * @author Bob McWhirter
 */
public class SimpleStompServer {

    public SimpleStompServer(String host, int port, ServerContext serverContext) {
        this.host = host;
        this.port = port;
        this.serverContext = serverContext;
        this.group = new NioEventLoopGroup();
    }

    public void start() throws InterruptedException {
        ServerBootstrap serverBootstrap = new ServerBootstrap();
        serverBootstrap
                .channel(NioServerSocketChannel.class)
                .group(this.group)
                .localAddress(this.host, this.port)
                .childHandler(createChildHandler());
        ChannelFuture future = serverBootstrap.bind();
        future.sync();
    }

    protected ChannelInitializer<NioSocketChannel> createChildHandler() {
        return new ChannelInitializer<NioSocketChannel>() {
            @Override
            protected void initChannel(NioSocketChannel ch) throws Exception {
                // bytes into frames
                ch.pipeline().addLast( new StompFrameDecoder() );
                ch.pipeline().addLast( new StompFrameEncoder() );
                // handle frames
                ch.pipeline().addLast( new ConnectHandler( SimpleStompServer.this.serverContext ) );
                ch.pipeline().addLast( new DisconnectHandler( SimpleStompServer.this.serverContext ) );
                ch.pipeline().addLast( new SubscribeHandler( SimpleStompServer.this.serverContext ) );
                ch.pipeline().addLast( new UnsubscribeHandler( SimpleStompServer.this.serverContext ) );
                // convert some frames to messages
                ch.pipeline().addLast( new ReceiptHandler() );
                ch.pipeline().addLast( new StompMessageDecoder() );
                ch.pipeline().addLast( new StompMessageEncoder(true) );
                // handle messages
                ch.pipeline().addLast( new SendHandler( SimpleStompServer.this.serverContext ) );
                // catch errors, return an ERROR message.
                ch.pipeline().addLast( new ErrorHandler() );
            }
        };
    }


    public void stop() throws InterruptedException {
        Future<?> future = this.group.shutdownGracefully();
        future.sync();
    }

    private final String host;
    private final int port;
    private ServerContext serverContext;
    private EventLoopGroup group;

}
