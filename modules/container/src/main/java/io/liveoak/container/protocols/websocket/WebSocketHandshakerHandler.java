/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.container.protocols.websocket;

import io.liveoak.container.protocols.PipelineConfigurator;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultHttpRequest;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshakerFactory;
import io.netty.util.ReferenceCountUtil;

/**
 * @author Bob McWhirter
 */
public class WebSocketHandshakerHandler extends SimpleChannelInboundHandler<Object> {

    public WebSocketHandshakerHandler(PipelineConfigurator configurator) {
        this.configurator = configurator;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (!(msg instanceof FullHttpRequest)) {
            DefaultHttpRequest req = (DefaultHttpRequest) msg;
            String upgrade = req.headers().get(HttpHeaders.Names.UPGRADE);
            if (HttpHeaders.Values.WEBSOCKET.equalsIgnoreCase(upgrade)) {
                // ensure FullHttpRequest by installing HttpObjectAggregator in front of this handler
                ReferenceCountUtil.retain(msg);
                this.configurator.switchToWebSocketsHandshake(ctx.pipeline());
                ctx.pipeline().fireChannelRead(msg);
            } else {
                ReferenceCountUtil.retain(msg);
                this.configurator.switchToPlainHttp(ctx.pipeline());
                ctx.pipeline().fireChannelRead(msg);
            }
        } else {
            // do the handshake
            FullHttpRequest req = (FullHttpRequest) msg;
            WebSocketServerHandshakerFactory wsFactory = new WebSocketServerHandshakerFactory(req.getUri(), null, false);
            WebSocketServerHandshaker handshaker = wsFactory.newHandshaker(req);
            if (handshaker == null) {
                WebSocketServerHandshakerFactory.sendUnsupportedVersionResponse(ctx.channel());
            } else {
                ChannelFuture future = handshaker.handshake(ctx.channel(), req);
                future.addListener(f -> {
                    this.configurator.switchToWebSockets(ctx.pipeline());
                });
            }
        }
    }

    private PipelineConfigurator configurator;
}
