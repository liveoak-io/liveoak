/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.container.server;

import io.liveoak.container.protocols.ProtocolDetector;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.nio.NioSocketChannel;

public class UnsecureServer extends AbstractNetworkServer {

    public UnsecureServer() {
        super();
    }

    protected ChannelHandler createChildHandler() {
        return new ChannelInitializer<NioSocketChannel>() {
            protected void initChannel(NioSocketChannel ch) throws Exception {
                ch.pipeline().addLast("protocol-detector", new ProtocolDetector(getPipelineConfigurator()));
            }
        };
    }
}
