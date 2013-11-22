/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.stomp.client.protocol;

import io.liveoak.stomp.Headers;
import io.liveoak.stomp.StompMessage;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;

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
                String subscriptionId = stompMessage.headers().get(Headers.SUBSCRIPTION);
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
