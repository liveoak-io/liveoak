/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.stomp.client.protocol;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.function.Consumer;

import io.liveoak.stomp.Headers;
import io.liveoak.stomp.Stomp;
import io.liveoak.stomp.StompMessage;
import io.liveoak.stomp.client.ReceiptReceiver;
import io.liveoak.stomp.client.SubscriptionImpl;
import io.liveoak.stomp.common.StompControlFrame;
import io.liveoak.stomp.common.StompFrame;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;

/**
 * @author Bob McWhirter
 */
public class ReceiptHandler extends ChannelDuplexHandler {

    public ReceiptHandler(Executor executor) {
        this.executor = executor;
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        if (msg instanceof ReceiptReceiver) {
            String receiptId = ((ReceiptReceiver) msg).receiptId();
            if (receiptId != null) {
                this.receiptReceivers.put(receiptId, (ReceiptReceiver) msg);
            }
        }
        super.write(ctx, msg, promise);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof StompControlFrame) {

            StompControlFrame frame = (StompControlFrame) msg;
            if (((StompControlFrame) msg).command().equals(Stomp.Command.RECEIPT)) {
                ReceiptReceiver receiver = this.receiptReceivers.remove(((StompControlFrame) msg).headers().get(Headers.RECEIPT_ID));
                if (receiver != null) {
                    this.executor.execute(() -> {
                        receiver.receivedReceipt();
                    });
                }
            }
        }
        super.channelRead(ctx, msg);
    }


    private Executor executor;
    private Map<String, ReceiptReceiver> receiptReceivers = new HashMap<>();
}
