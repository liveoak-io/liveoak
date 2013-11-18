/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.container.metrics;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;

/**
 * @author Bob McWhirter
 */
public class MetricsHandler extends ChannelDuplexHandler {

    @Override
    public void write( ChannelHandlerContext ctx, Object msg, ChannelPromise promise ) throws Exception {
        super.write( ctx, msg, promise );    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    public void channelRead( ChannelHandlerContext ctx, Object msg ) throws Exception {
        super.channelRead( ctx, msg );    //To change body of overridden methods use File | Settings | File Templates.
    }
}
