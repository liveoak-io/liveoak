package io.liveoak.stomp.client.protocol;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.liveoak.stomp.Headers;
import io.liveoak.stomp.StompMessage;

import java.util.concurrent.Executor;
import java.util.function.Consumer;

/**
 * @author Bob McWhirter
 */
public class MessageHandler extends ChannelDuplexHandler {

    public MessageHandler(StompClientContext clientContext, Executor executor) {
        this.clientContext = clientContext;
        this.executor = executor;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof StompMessage) {
            StompMessage stompMessage = (StompMessage) msg;
            if (stompMessage.isError()) {

            } else {
                String subscriptionId = stompMessage.headers().get( Headers.SUBSCRIPTION );
                this.clientContext.getSubscriptionHandler(subscriptionId);
                Consumer<StompMessage> handler = this.clientContext.getSubscriptionHandler(subscriptionId);
                this.executor.execute(() -> {
                    handler.accept(stompMessage);
                });
            }
        } else {
            super.channelRead(ctx, msg);
        }
    }


    private Executor executor;
    private StompClientContext clientContext;
}
