package org.projectodd.restafari.stomp.server.protocol;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.projectodd.restafari.stomp.Headers;
import org.projectodd.restafari.stomp.common.StompFrame;

/**
 * @author Bob McWhirter
 */
public class ReceiptHandler extends SimpleChannelInboundHandler<StompFrame> {

    public ReceiptHandler() {
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, StompFrame msg) throws Exception {
        String receiptId = msg.headers().get( Headers.RECEIPT );
        if ( receiptId != null ) {
            ctx.writeAndFlush(StompFrame.newReceiptFrame(receiptId));
        }
        // always pass it upstream
        ctx.fireChannelRead( msg );
    }

}
