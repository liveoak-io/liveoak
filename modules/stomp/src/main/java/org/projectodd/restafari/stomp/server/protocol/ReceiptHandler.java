package org.projectodd.restafari.stomp.server.protocol;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.projectodd.restafari.stomp.Headers;
import org.projectodd.restafari.stomp.Stomp;
import org.projectodd.restafari.stomp.StompMessage;
import org.projectodd.restafari.stomp.common.AbstractControlFrameHandler;
import org.projectodd.restafari.stomp.common.AbstractFrameHandler;
import org.projectodd.restafari.stomp.common.StompControlFrame;
import org.projectodd.restafari.stomp.common.StompFrame;
import org.projectodd.restafari.stomp.server.ServerContext;
import org.projectodd.restafari.stomp.server.StompConnection;

/**
 * @author Bob McWhirter
 */
public class ReceiptHandler extends SimpleChannelInboundHandler<StompMessage> {

    public ReceiptHandler() {
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, StompMessage msg) throws Exception {
        String receiptId = msg.getHeaders().get( Headers.RECEIPT );
        if ( receiptId != null ) {
            ctx.writeAndFlush(StompFrame.newReceiptFrame(receiptId));
        }
    }

}
