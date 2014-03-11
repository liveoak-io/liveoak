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
import io.liveoak.stomp.server.StompServerSecurityException;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.jboss.logging.Logger;

/**
 * @author Bob McWhirter
 */
public class StompErrorHandler extends ChannelDuplexHandler {

    private static final Logger log = Logger.getLogger(StompErrorHandler.class);

    public StompErrorHandler() {

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        int status = HttpResponseStatus.INTERNAL_SERVER_ERROR.code();

        if (cause instanceof StompServerSecurityException) {
            StompServerSecurityException securityException = (StompServerSecurityException) cause;
            log.warn("Not authorized. status: " + securityException.getStatus() +
                    ", command: " + securityException.getCommand() +
                    ", receiptId: " + securityException.getReceiptId() +
                    ", message: " + securityException.getMessage());
            status = securityException.getStatus();
        } else {
            log.error("", cause);
        }
        StompMessage errorMessage = new DefaultStompMessage(true);

        errorMessage.headers().put("status", String.valueOf(status));

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
