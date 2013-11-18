/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.stomp.server.protocol;

import io.liveoak.stomp.Headers;
import io.liveoak.stomp.Stomp;
import io.liveoak.stomp.common.AbstractControlFrameHandler;
import io.liveoak.stomp.common.StompControlFrame;
import io.liveoak.stomp.server.StompConnection;
import io.liveoak.stomp.server.StompServerContext;
import io.liveoak.stomp.server.StompServerException;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;

/**
 * @author Bob McWhirter
 */
public class DisconnectHandler extends AbstractControlFrameHandler {

    public DisconnectHandler( StompServerContext serverContext ) {
        super( Stomp.Command.DISCONNECT );
        this.serverContext = serverContext;
    }

    @Override
    protected void handleControlFrame( ChannelHandlerContext ctx, StompControlFrame frame ) throws StompServerException {
        StompConnection stompConnection = ctx.channel().attr( ConnectHandler.CONNECTION ).get();
        this.serverContext.handleDisconnect( stompConnection );
        String receiptId = frame.headers().get( Headers.RECEIPT );
        ChannelFuture future = ctx.writeAndFlush( StompControlFrame.newReceiptFrame( receiptId ) );
        future.addListener( ( f ) -> {
            ctx.close();
        } );
    }

    private StompServerContext serverContext;
}
