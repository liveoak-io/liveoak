/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.stomp.server.protocol;

import io.liveoak.stomp.Headers;
import io.liveoak.stomp.StompMessage;
import io.liveoak.stomp.common.DefaultStompMessage;
import io.liveoak.stomp.server.StompServerException;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;

/**
 * @author Bob McWhirter
 */
public class ErrorHandler extends ChannelDuplexHandler {

    public ErrorHandler() {

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        StompMessage errorMessage = new DefaultStompMessage(true);

        errorMessage.headers().put("status", "500");

        if (cause instanceof StompServerException) {
            errorMessage.content(cause.getMessage());
            String receiptId = ((StompServerException) cause).getReceiptId();
            if (receiptId != null) {
                errorMessage.headers().put(Headers.RECEIPT_ID, receiptId);
            }
        } else {
            errorMessage.content("An internal error has occurred.");
        }
        ctx.writeAndFlush(errorMessage);
    }

}
