/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.container.server;

import io.liveoak.spi.container.Server;
import io.liveoak.container.protocols.PipelineConfigurator;
import io.liveoak.spi.Container;
import io.liveoak.stomp.common.DebugHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.ServerChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.concurrent.Future;

import java.net.InetAddress;
import java.net.SocketAddress;
import java.nio.channels.Channel;

/**
 * Base networkServer capable of connecting a container to a network ports.
 *
 * @author Bob McWhirter
 */
public abstract class AbstractServer implements Server {

    public AbstractServer() {
    }

    protected abstract EventLoopGroup eventLoopGroup();
    protected abstract Class<? extends ServerChannel> channelClass();
    public abstract SocketAddress localAddress();

    public void pipelineConfigurator(PipelineConfigurator pipelineConfigurator) {
        this.pipelineConfigurator = pipelineConfigurator;
    }

    public PipelineConfigurator pipelineConfigurator() {
        return this.pipelineConfigurator;
    }

    /**
     * Synchronously start the network listener.
     *
     * @throws InterruptedException If interrupted before completely starting.
     */
    public void start() throws InterruptedException {
        ServerBootstrap serverBootstrap = new ServerBootstrap();
        serverBootstrap
                .channel(channelClass())
                .group(eventLoopGroup())
                .localAddress(localAddress())
                //.handler( new DebugHandler( "server-handler" ) )
                .childHandler(createChildHandler());
        ChannelFuture future = serverBootstrap.bind();
        future.sync();
    }

    /**
     * Synchronously stop the network listener.
     *
     * @throws InterruptedException If interrupted before completely stopping.
     */
    public void stop() throws InterruptedException {
        Future<?> future = eventLoopGroup().shutdownGracefully();
        future.sync();
    }

    protected PipelineConfigurator getPipelineConfigurator() {
        return this.pipelineConfigurator;
    }

    /**
     * Create a networkServer-specific port-handler.
     *
     * <p>This is implemented by concrete subclasses to provide
     * SSL or bare networking handling.</p>
     *
     * @return The channel-handler for the netowrk listener.
     */
    protected abstract ChannelHandler createChildHandler();

    private PipelineConfigurator pipelineConfigurator;

}
