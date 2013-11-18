/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.container.protocols.websocket;

import io.liveoak.container.PipelineConfigurator;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshakerFactory;
import io.netty.util.ReferenceCountUtil;

/**
 * @author Bob McWhirter
 */
public class WebSocketHandshakerHandler extends SimpleChannelInboundHandler<Object> {

    public WebSocketHandshakerHandler( PipelineConfigurator configurator ) {
        this.configurator = configurator;
    }

    @Override
    protected void channelRead0( ChannelHandlerContext ctx, Object msg ) throws Exception {

        FullHttpRequest req = ( FullHttpRequest ) msg;
        String upgrade = req.headers().get( HttpHeaders.Names.UPGRADE );
        if ( HttpHeaders.Values.WEBSOCKET.equalsIgnoreCase( upgrade ) ) {
            WebSocketServerHandshakerFactory wsFactory = new WebSocketServerHandshakerFactory( req.getUri(), null, false );
            WebSocketServerHandshaker handshaker = wsFactory.newHandshaker( req );
            if ( handshaker == null ) {
                WebSocketServerHandshakerFactory.sendUnsupportedWebSocketVersionResponse( ctx.channel() );
            } else {
                ChannelFuture future = handshaker.handshake( ctx.channel(), req );
                future.addListener( f -> {
                    this.configurator.switchToWebSockets( ctx.pipeline() );
                } );
            }
        } else {
            ReferenceCountUtil.retain( msg );
            this.configurator.switchToPlainHttp( ctx.pipeline() );
            ChannelHandlerContext agg = ctx.pipeline().context( HttpObjectAggregator.class );
            agg.fireChannelRead( msg );
        }
    }

    private PipelineConfigurator configurator;
}
