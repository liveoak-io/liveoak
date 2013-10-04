package org.projectodd.restafari.container.protocols.websocket;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshakerFactory;
import io.netty.util.ReferenceCountUtil;
import org.projectodd.restafari.container.protocols.PipelineConfigurator;

/**
 * @author Bob McWhirter
 */
public class WebSocketHandshakerHandler extends SimpleChannelInboundHandler<Object> {

    private static final String WEBSOCKETS_UPGRADE = "websocket";

    public WebSocketHandshakerHandler(PipelineConfigurator configurator) {
        this.configurator = configurator;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {

        FullHttpRequest req = (FullHttpRequest) msg;
        String upgrade = req.headers().get(HttpHeaders.Names.UPGRADE);
        if (WEBSOCKETS_UPGRADE.equals(upgrade)) {
            WebSocketServerHandshakerFactory wsFactory = new WebSocketServerHandshakerFactory(req.getUri(), null, false);
            WebSocketServerHandshaker handshaker = wsFactory.newHandshaker(req);
            if (handshaker == null) {
                WebSocketServerHandshakerFactory.sendUnsupportedWebSocketVersionResponse(ctx.channel());
            } else {
                ChannelFuture future = handshaker.handshake(ctx.channel(), req);
                future.addListener(f -> {
                    this.configurator.switchToWebSockets(ctx.pipeline());
                });
            }
        } else {
            ReferenceCountUtil.retain( msg );
            this.configurator.switchToPlainHttp(ctx.pipeline());
            ChannelHandlerContext agg = ctx.pipeline().context(HttpObjectAggregator.class);
            agg.fireChannelRead( msg );
        }
    }

    private PipelineConfigurator configurator;
}
