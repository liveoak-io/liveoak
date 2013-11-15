/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.stomp.server.protocol;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.liveoak.stomp.StompMessage;
import io.liveoak.stomp.server.StompServerContext;
import io.liveoak.stomp.server.StompConnection;

/**
 * @author Bob McWhirter
 */
public class SendHandler extends SimpleChannelInboundHandler<StompMessage> {


    public SendHandler(StompServerContext serverContext) {
        this.serverContext = serverContext;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, StompMessage msg) throws Exception {
        StompMessage stompMessage = (StompMessage) msg;
        StompConnection connection = ctx.channel().attr(ConnectHandler.CONNECTION).get();
        this.serverContext.handleSend(connection, stompMessage);

        // end of the line, do NOT retain or send upstream.
    }

    private StompServerContext serverContext;

}
