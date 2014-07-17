/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.stomp.client.protocol;

import java.util.List;

import io.liveoak.stomp.Headers;
import io.liveoak.stomp.Stomp;
import io.liveoak.stomp.client.SubscriptionImpl;
import io.liveoak.stomp.common.StompControlFrame;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;

/**
 * @author Bob McWhirter
 */
public class SubscriptionEncoder extends MessageToMessageEncoder<SubscriptionImpl> {

    public SubscriptionEncoder() {
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, SubscriptionImpl msg, List<Object> out) throws Exception {
        StompControlFrame frame = new StompControlFrame(Stomp.Command.SUBSCRIBE);
        frame.headers().putAll(msg.headers());
        frame.headers().put(Headers.ID, msg.subscriptionId());
        frame.headers().put(Headers.DESTINATION, msg.destination());
        frame.headers().put(Headers.RECEIPT, msg.subscriptionId());
        out.add(frame);
    }

    private boolean server;
}
