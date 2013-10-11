package org.projectodd.restafari.stomp.client.protocol;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import org.projectodd.restafari.stomp.*;
import org.projectodd.restafari.stomp.common.AbstractFrameHandler;
import org.projectodd.restafari.stomp.common.DefaultStompMessage;
import org.projectodd.restafari.stomp.common.StompContentFrame;
import org.projectodd.restafari.stomp.common.StompFrame;

import java.util.concurrent.Executor;
import java.util.function.Consumer;

/**
 * @author Bob McWhirter
 */
public class MessageHandler extends ChannelDuplexHandler {

    public MessageHandler(ClientContext clientContext, Executor executor) {
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
    private ClientContext clientContext;
}
