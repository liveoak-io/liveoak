/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.container.server;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

import io.liveoak.container.protocols.PipelineConfigurator;
import io.liveoak.spi.container.NetworkServer;
import io.liveoak.spi.container.Server;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.ServerChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.concurrent.Future;

/**
 * Base networkServer capable of connecting a container to a network ports.
 *
 * @author Bob McWhirter
 */
public abstract class AbstractNetworkServer extends AbstractServer implements NetworkServer {

    public AbstractNetworkServer() {
        this.group = new NioEventLoopGroup();
    }

    @Override
    protected EventLoopGroup eventLoopGroup() {
        return this.group;
    }

    @Override
    protected Class<? extends ServerChannel> channelClass() {
        return NioServerSocketChannel.class;
    }

    @Override
    public SocketAddress localAddress() {
        return new InetSocketAddress( this.host, this.port );
    }

    @Override
    public void host(InetAddress host) {
        this.host = host;
    }

    @Override
    public InetAddress host() {
        return this.host;
    }

    @Override
    public void port(int port) {
        this.port = port;
    }

    @Override
    public int port() {
        return this.port;
    }

    private int port;
    private InetAddress host;
    private EventLoopGroup group;
}
