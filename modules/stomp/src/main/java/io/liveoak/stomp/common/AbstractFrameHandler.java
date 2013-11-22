/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.stomp.common;

import io.liveoak.stomp.Stomp;
import io.liveoak.stomp.server.StompServerException;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.ReferenceCountUtil;

/**
 * @author Bob McWhirter
 */
public abstract class AbstractFrameHandler extends SimpleChannelInboundHandler<StompFrame> {

    public AbstractFrameHandler(Stomp.Command command) {
        this.command = command;
    }

    public void channelRead0(ChannelHandlerContext ctx, StompFrame msg) throws Exception {
        if (this.command != null) {
            if (((StompFrame) msg).command().equals(this.command)) {
                handleFrame(ctx, (StompFrame) msg);
                return;
            }
        } else {
            handleFrame(ctx, (StompFrame) msg);
            return;
        }

        ReferenceCountUtil.retain(msg);
        ctx.fireChannelRead(msg);
    }

    protected abstract void handleFrame(ChannelHandlerContext ctx, StompFrame frame) throws StompServerException;

    private Stomp.Command command;
}
