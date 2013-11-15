/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.stomp.server.protocol;

import io.netty.channel.ChannelHandlerContext;
import io.netty.util.ReferenceCountUtil;
import io.liveoak.stomp.Headers;
import io.liveoak.stomp.Stomp;
import io.liveoak.stomp.common.AbstractControlFrameHandler;
import io.liveoak.stomp.common.StompControlFrame;
import io.liveoak.stomp.server.StompServerContext;
import io.liveoak.stomp.server.StompConnection;
import io.liveoak.stomp.server.StompServerException;

/**
 * @author Bob McWhirter
 */
public class UnsubscribeHandler extends AbstractControlFrameHandler {

    public UnsubscribeHandler(StompServerContext serverContext) {
        super(Stomp.Command.UNSUBSCRIBE);
        this.serverContext = serverContext;

    }

    @Override
    public void handleControlFrame(ChannelHandlerContext ctx, StompControlFrame msg) throws StompServerException {
        String subscriptionId = msg.headers().get(Headers.ID);
        StompConnection stompConnection = ctx.channel().attr( ConnectHandler.CONNECTION ).get();
        this.serverContext.handleUnsubscribe(stompConnection, subscriptionId);

        // retain and send upstream for RECEIPT
        ReferenceCountUtil.retain( msg );
        ctx.fireChannelRead(msg);
    }

    private StompServerContext serverContext;
}
