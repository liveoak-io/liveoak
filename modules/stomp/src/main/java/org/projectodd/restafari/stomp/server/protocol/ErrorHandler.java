package org.projectodd.restafari.stomp.server.protocol;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import org.projectodd.restafari.stomp.Headers;
import org.projectodd.restafari.stomp.Stomp;
import org.projectodd.restafari.stomp.StompMessage;
import org.projectodd.restafari.stomp.common.AbstractControlFrameHandler;
import org.projectodd.restafari.stomp.common.DefaultStompMessage;
import org.projectodd.restafari.stomp.common.StompControlFrame;
import org.projectodd.restafari.stomp.server.ServerContext;
import org.projectodd.restafari.stomp.server.StompConnection;
import org.projectodd.restafari.stomp.server.StompServerException;

/**
 * @author Bob McWhirter
 */
public class ErrorHandler extends ChannelDuplexHandler {

    public ErrorHandler() {

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        StompMessage errorMessage = new DefaultStompMessage(true);

        errorMessage.getHeaders().put( "status", "500" );

        if (cause instanceof StompServerException) {
            errorMessage.setContentAsString( cause.getMessage() );
            String receiptId = ((StompServerException) cause).getReceiptId();
            if (receiptId != null) {
                errorMessage.getHeaders().put(Headers.RECEIPT_ID, receiptId);
            }
        } else {
            errorMessage.setContentAsString( "An internal error has occurred." );
        }
        //super.exceptionCaught(ctx, cause);
    }

}
