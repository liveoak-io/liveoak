/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.stomp.client.protocol;

import io.liveoak.stomp.Headers;
import io.liveoak.stomp.StompMessage;
import io.liveoak.stomp.client.Subscription;
import io.liveoak.stomp.client.SubscriptionImpl;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.function.Consumer;

/**
 * @author Bob McWhirter
 */
public class MessageHandler extends ChannelDuplexHandler {

    public MessageHandler(Executor executor) {
        this.executor = executor;
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        if (msg instanceof SubscriptionImpl) {
            this.subscriptions.put(((SubscriptionImpl) msg).subscriptionId(), (SubscriptionImpl) msg);
        }
        super.write(ctx, msg, promise);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof StompMessage) {
            StompMessage stompMessage = (StompMessage) msg;
            String subscriptionId = stompMessage.headers().get(Headers.SUBSCRIPTION);
            SubscriptionImpl subscription = this.subscriptions.get( subscriptionId );
            if ( subscription != null ) {
                this.executor.execute(() -> {
                    Consumer<StompMessage> onMessage = subscription.onMessage();
                    if ( onMessage != null ) {
                        onMessage.accept( stompMessage );
                    }
                });
            } else {
                super.channelRead(ctx, msg);
            }
        } else {
            super.channelRead(ctx, msg);
        }
    }


    private Executor executor;
    private Map<String, SubscriptionImpl> subscriptions = new HashMap<>();
}
