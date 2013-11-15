/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.stomp.server.protocol;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.ReferenceCountUtil;
import io.liveoak.stomp.Headers;
import io.liveoak.stomp.common.StompFrame;

/**
 * @author Bob McWhirter
 */
public class ReceiptHandler extends SimpleChannelInboundHandler<StompFrame> {

    public ReceiptHandler() {
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, StompFrame msg) throws Exception {
        String receiptId = msg.headers().get( Headers.RECEIPT );
        if ( receiptId != null ) {
            ctx.writeAndFlush(StompFrame.newReceiptFrame(receiptId));
        }

        // retain and keep it moving upstream
        ReferenceCountUtil.retain( msg );
        ctx.fireChannelRead( msg );
    }

}
