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
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.ReferenceCountUtil;

/**
 * @author Bob McWhirter
 */
public class SubscribeHandler extends AbstractControlFrameHandler {

    public SubscribeHandler( StompServerContext serverContext ) {
        super( Stomp.Command.SUBSCRIBE );
        this.serverContext = serverContext;
    }

    @Override
    public void handleControlFrame( ChannelHandlerContext ctx, StompControlFrame msg ) throws StompServerException {
        String subscriptionId = msg.headers().get( Headers.ID );
        String destination = msg.headers().get( Headers.DESTINATION );
        StompConnection stompConnection = ctx.channel().attr( ConnectHandler.CONNECTION ).get();
        this.serverContext.handleSubscribe( stompConnection, destination, subscriptionId, msg.headers() );

        // retain and send upstream for RECEIPT
        ReferenceCountUtil.retain( msg );
        ctx.fireChannelRead( msg );
    }

    private StompServerContext serverContext;
}
