/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */

package io.liveoak.stomp.client.protocol;

import java.util.function.Consumer;

import io.liveoak.stomp.StompMessage;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class StompErrorClientHandler extends SimpleChannelInboundHandler<StompMessage> {

    private Consumer<StompMessage> errorConsumer;

    public StompErrorClientHandler(Consumer<StompMessage> errorConsumer) {
        this.errorConsumer = errorConsumer;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, StompMessage msg) throws Exception {
        if (msg.isError()) {
            errorConsumer.accept(msg);
        }
    }
}
