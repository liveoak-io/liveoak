/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.container;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.concurrent.Future;

import java.net.InetAddress;

/** Base server capable of connecting a container to a network ports.
 *
 * @author Bob McWhirter
 */
public abstract class AbstractServer {

    public AbstractServer( DefaultContainer container, InetAddress host, int port, EventLoopGroup group ) {
        this.container = container;
        this.host = host;
        this.port = port;
        this.group = group;
        this.pipelineConfigurator = new PipelineConfigurator( this.container );
    }

    /** Synchronously start the network listener.
     *
     * @throws InterruptedException If interrupted before completely starting.
     */
    public void start() throws InterruptedException {
        ServerBootstrap serverBootstrap = new ServerBootstrap();
        serverBootstrap
                .channel( NioServerSocketChannel.class )
                .group( this.group )
                .localAddress( this.host, this.port )
                .childHandler( createChildHandler() );
        ChannelFuture future = serverBootstrap.bind();
        future.sync();
    }

    /** Synchronously stop the network listener.
     *
     * @throws InterruptedException If interrupted before completely stopping.
     */
    public void stop() throws InterruptedException {
        Future<?> future = this.group.shutdownGracefully();
        future.sync();
        this.container.shutdown();
    }

    protected PipelineConfigurator getPipelineConfigurator() {
        return this.pipelineConfigurator;
    }

    public DefaultContainer container() {
        return this.container;
    }

    /** Create a server-specific port-handler.
     *
     * <p>This is implemented by concrete subclasses to provide
     * SSL or bare networking handling.</p>
     *
     * @return The channel-handler for the netowrk listener.
     */
    protected abstract ChannelHandler createChildHandler();

    private DefaultContainer container;
    private int port;
    private InetAddress host;
    private EventLoopGroup group;
    private final PipelineConfigurator pipelineConfigurator;

}
