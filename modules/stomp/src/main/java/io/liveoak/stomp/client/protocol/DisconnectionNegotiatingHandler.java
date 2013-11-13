package io.liveoak.stomp.client.protocol;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.liveoak.stomp.Headers;
import io.liveoak.stomp.Stomp;
import io.liveoak.stomp.client.StompClient;
import io.liveoak.stomp.common.StompControlFrame;

/**
 * @author Bob McWhirter
 */
public class DisconnectionNegotiatingHandler extends ChannelDuplexHandler {

    public DisconnectionNegotiatingHandler(StompClientContext clientContext) {
        this.clientContext = clientContext;
    }

    public void setCallback(Runnable callback) {
        this.callback = callback;
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        if (msg instanceof StompControlFrame && ((StompControlFrame) msg).command() == Stomp.Command.DISCONNECT) {
            this.receiptId = ((StompControlFrame) msg).headers().get(Headers.RECEIPT);
            this.clientContext.setConnectionState(StompClient.ConnectionState.DISCONNECTING);
        }
        super.write(ctx, msg, promise);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (this.receiptId != null) {
            if (msg instanceof StompControlFrame) {
                String receiptId = ((StompControlFrame) msg).headers().get(Headers.RECEIPT_ID);
                if (receiptId.equals(this.receiptId)) {
                    this.clientContext.setConnectionState(StompClient.ConnectionState.DISCONNECTED);
                    ctx.close();
                    if (this.callback != null) {
                        this.callback.run();
                    }
                }
            }
        }
        super.channelRead(ctx, msg);
    }

    private StompClientContext clientContext;
    private String receiptId;
    private Runnable callback;
}
