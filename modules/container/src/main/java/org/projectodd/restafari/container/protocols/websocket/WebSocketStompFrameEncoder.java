package org.projectodd.restafari.container.protocols.websocket;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import org.projectodd.restafari.container.protocols.stomp.StompFrameEncoder;

import java.util.List;

/** STOMP frame to WebSocket frame encoder
 *
 * This encoder also adds the typical STOMP frame encoder upstream of itself,
 * providing for the pipeline of:
 *
 * STOMP frame to bytes to WebSocketFrame
 *
 * @author Bob McWhirter
 */
public class WebSocketStompFrameEncoder extends MessageToMessageEncoder<ByteBuf> {

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        ctx.pipeline().addAfter( ctx.name(), "stomp-frame-encoder", new StompFrameEncoder() );
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out) throws Exception {
        out.add( new TextWebSocketFrame( msg ) );
    }

}
