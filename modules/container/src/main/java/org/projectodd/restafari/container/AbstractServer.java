package org.projectodd.restafari.container;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.util.concurrent.Future;

import java.net.InetAddress;

import org.projectodd.restafari.container.http.HttpContainerHandler;
import org.projectodd.restafari.container.http.HttpNoSuchCollectionResponseEncoder;
import org.projectodd.restafari.container.http.HttpNoSuchResourceResponseEncoder;
import org.projectodd.restafari.container.http.HttpResourceResponseEncoder;
import org.projectodd.restafari.container.http.HttpResourcesResponseEncoder;

public abstract class AbstractServer {

    public AbstractServer(Container container, InetAddress host, int port, EventLoopGroup group) {
        this.container = container;
        this.host = host;
        this.port = port;
        this.group = group;
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

    public void stop() throws InterruptedException {
        System.err.println("stopping");
        Future<?> future = this.group.shutdownGracefully();
        future.sync();
        System.err.println("stopped");
    }

    protected abstract ChannelHandler createChildHandler();

    protected void addHttpCodec(ChannelPipeline pipeline) {
        pipeline.addLast(new HttpRequestDecoder());
        pipeline.addLast(new HttpResponseEncoder());
    }

    protected void addHttpResourceResponseEncoders(ChannelPipeline pipeline) {
        pipeline.addLast(new HttpResourceResponseEncoder(this.container.getCodecManager()));
        pipeline.addLast(new HttpResourcesResponseEncoder(this.container.getCodecManager()));
        pipeline.addLast(new HttpNoSuchCollectionResponseEncoder(this.container.getCodecManager()));
        pipeline.addLast(new HttpNoSuchResourceResponseEncoder(this.container.getCodecManager()));
    }

    protected void addHttpContainerHandler(ChannelPipeline pipeline) {
        pipeline.addLast(new HttpContainerHandler(this.container));
    }

    private Container container;
    private int port;
    private InetAddress host;
    private EventLoopGroup group;

}
