package org.projectodd.restafari.stomp.server.protocol;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import org.projectodd.restafari.stomp.Headers;
import org.projectodd.restafari.stomp.StompMessage;
import org.projectodd.restafari.stomp.common.DefaultStompMessage;
import org.projectodd.restafari.stomp.server.StompServerException;

/**
 * @author Bob McWhirter
 */
public class ErrorHandler extends ChannelDuplexHandler {

    public ErrorHandler() {

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        StompMessage errorMessage = new DefaultStompMessage(true);

        errorMessage.headers().put("status", "500");

        if (cause instanceof StompServerException) {
            errorMessage.content(cause.getMessage());
            String receiptId = ((StompServerException) cause).getReceiptId();
            if (receiptId != null) {
                errorMessage.headers().put(Headers.RECEIPT_ID, receiptId);
            }
        } else {
            errorMessage.content("An internal error has occurred.");
        }
        ctx.writeAndFlush( errorMessage );
    }

}
