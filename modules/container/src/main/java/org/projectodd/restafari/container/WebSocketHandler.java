package org.projectodd.restafari.container;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshakerFactory;
import io.netty.util.ReferenceCountUtil;

/**
 * @author Bob McWhirter
 */
public class WebSocketHandler extends SimpleChannelInboundHandler<Object> {

    private static final String WEBSOCKETS_PREFIX = "/_websockets";
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {

        FullHttpRequest req = (FullHttpRequest) msg;

        if (req.getUri().startsWith( WEBSOCKETS_PREFIX ) ) {
            WebSocketServerHandshakerFactory wsFactory = new WebSocketServerHandshakerFactory(WEBSOCKETS_PREFIX, null, false);
            WebSocketServerHandshaker handshaker = wsFactory.newHandshaker(req);
            if (handshaker == null) {
                WebSocketServerHandshakerFactory.sendUnsupportedWebSocketVersionResponse(ctx.channel());
            } else {
                handshaker.handshake(ctx.channel(), req);
            }
        } else {
            ReferenceCountUtil.retain(msg);
            ctx.fireChannelRead( req );
        }
    }
}
