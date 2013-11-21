/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.container.protocols.websocket;

import io.liveoak.stomp.common.StompFrameEncoder;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

import java.util.List;

/**
 * STOMP frame to WebSocket frame encoder
 * <p>This encoder also adds the typical STOMP frame encoder upstream of itself,
 * providing for the pipeline of:</p>
 *
 * <p>STOMP frame to bytes to WebSocketFrame</p>
 *
 * @author Bob McWhirter
 */
public class WebSocketStompFrameEncoder extends MessageToMessageEncoder<ByteBuf> {

    @Override
    public void handlerAdded( ChannelHandlerContext ctx ) throws Exception {
        ctx.pipeline().addAfter( ctx.name(), "stomp-frame-encoder", new StompFrameEncoder() );
    }

    @Override
    protected void encode( ChannelHandlerContext ctx, ByteBuf msg, List<Object> out ) throws Exception {
        out.add( new TextWebSocketFrame( msg.retain() ) );
    }

}
