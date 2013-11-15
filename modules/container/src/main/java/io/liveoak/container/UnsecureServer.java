/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.container;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.liveoak.container.protocols.ProtocolDetector;
import org.vertx.java.core.Vertx;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class UnsecureServer extends AbstractServer {

    public UnsecureServer(Vertx vertx, String host, int port) throws UnknownHostException {
        super(new DefaultContainer(vertx), InetAddress.getByName(host), port, new NioEventLoopGroup());
    }

    public UnsecureServer(DefaultContainer container, String host, int port) throws UnknownHostException {
        super(container, InetAddress.getByName(host), port, new NioEventLoopGroup());
    }

    public UnsecureServer(DefaultContainer container, InetAddress host, int port) {
        super(container, host, port, new NioEventLoopGroup());
    }

    public UnsecureServer(DefaultContainer container, InetAddress host, int port, EventLoopGroup group) {
        super(container, host, port, group);
    }

    protected ChannelHandler createChildHandler() {
        
        return new ChannelInitializer<NioSocketChannel>() {
            protected void initChannel(NioSocketChannel ch) throws Exception {
                ch.pipeline().addLast(new ProtocolDetector(getPipelineConfigurator()));
            }
        };
    }


}
