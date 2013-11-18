/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.stomp.common;

import io.liveoak.stomp.Stomp;
import io.liveoak.stomp.StompMessage;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import io.netty.util.ReferenceCountUtil;

import java.util.List;

/**
 * @author Bob McWhirter
 */
public class StompMessageDecoder extends MessageToMessageDecoder<StompFrame> {

    @Override
    protected void decode( ChannelHandlerContext ctx, StompFrame msg, List<Object> out ) throws Exception {
        if ( msg instanceof StompContentFrame ) {
            if ( msg.command() == Stomp.Command.MESSAGE || msg.command() == Stomp.Command.SEND ) {
                StompMessage stompMessage = new DefaultStompMessage( msg.headers(), ( ( StompContentFrame ) msg ).content().retain() );
                out.add( stompMessage );
            } else {
                ReferenceCountUtil.retain( msg );
                out.add( msg );
            }
        } else {
            ReferenceCountUtil.retain( msg );
            out.add( msg );
        }

    }
}
