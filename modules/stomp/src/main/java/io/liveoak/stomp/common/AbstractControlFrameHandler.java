/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.stomp.common;

import io.liveoak.stomp.Stomp;
import io.liveoak.stomp.server.StompServerException;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.ReferenceCountUtil;

/**
 * @author Bob McWhirter
 */
public abstract class AbstractControlFrameHandler extends AbstractFrameHandler {

    public AbstractControlFrameHandler( Stomp.Command command ) {
        super( command );
    }

    public void handleFrame( ChannelHandlerContext ctx, StompFrame msg ) throws StompServerException {
        if ( msg instanceof StompControlFrame ) {
            handleControlFrame( ctx, ( StompControlFrame ) msg );
            return;
        }

        ReferenceCountUtil.retain( msg );
        ctx.fireChannelRead( msg );
    }

    protected abstract void handleControlFrame( ChannelHandlerContext ctx, StompControlFrame frame ) throws StompServerException;
}
