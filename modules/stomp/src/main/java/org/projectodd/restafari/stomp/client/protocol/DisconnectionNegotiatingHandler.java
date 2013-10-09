package org.projectodd.restafari.stomp.client.protocol;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import org.projectodd.restafari.stomp.Headers;
import org.projectodd.restafari.stomp.Stomp;
import org.projectodd.restafari.stomp.client.ClientContext;
import org.projectodd.restafari.stomp.common.StompControlFrame;

/**
 * @author Bob McWhirter
 */
public class DisconnectionNegotiatingHandler extends ChannelDuplexHandler {

    public DisconnectionNegotiatingHandler(ClientContext clientContext) {
        this.clientContext = clientContext;
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        if ( msg instanceof StompControlFrame && ((StompControlFrame) msg).getCommand() == Stomp.Command.DISCONNECT ) {
            this.receiptId = ((StompControlFrame) msg).getHeader( Headers.RECEIPT );
            this.clientContext.setConnectionState( ClientContext.State.DISCONNECTING );
        }
        super.write(ctx, msg, promise);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if ( this.receiptId != null ) {
            System.err.println( "look for receipt: " + this.receiptId );
            if ( msg instanceof StompControlFrame ) {
                String receiptId = ((StompControlFrame) msg).getHeader( Headers.RECEIPT_ID );
                System.err.println( "found receipt: " + receiptId );
                if ( receiptId.equals( this.receiptId ) ) {
                    System.err.println( "set state to disconnected" );
                    this.clientContext.setConnectionState(ClientContext.State.DISCONNECTED);
                    ctx.close();
                }
            }
        }
        super.channelRead(ctx, msg);
    }

    private ClientContext clientContext;
    private String receiptId;
}
