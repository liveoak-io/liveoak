/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.container;

import io.liveoak.common.DefaultResourceErrorResponse;
import io.liveoak.spi.ResourceErrorResponse;
import io.liveoak.spi.ResourceRequest;
import io.liveoak.spi.exceptions.ForbiddenException;
import io.liveoak.spi.exceptions.NotAcceptableException;
import io.liveoak.spi.exceptions.NotAuthorizedException;
import io.liveoak.spi.exceptions.PropertyException;
import io.liveoak.spi.exceptions.ResourceAlreadyExistsException;
import io.liveoak.spi.exceptions.ResourceNotFoundException;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import org.jboss.logging.Logger;

public class ErrorHandler extends ChannelDuplexHandler {

    private static Logger log = Logger.getLogger(ErrorHandler.class);

    public ErrorHandler() {
    }

    public static void handleError(ChannelHandlerContext ctx, ResourceRequest inReplyTo, Throwable t) {
        ResourceErrorResponse.ErrorType errorType;
        Logger.Level level = Logger.Level.TRACE;

        if (t instanceof NotAuthorizedException) {
            errorType = ResourceErrorResponse.ErrorType.NOT_AUTHORIZED;
        } else if (t instanceof ForbiddenException) {
            errorType = ResourceErrorResponse.ErrorType.FORBIDDEN;
        } else if (t instanceof NotAcceptableException) {
            errorType = ResourceErrorResponse.ErrorType.NOT_ACCEPTABLE;
        } else if (t instanceof ResourceNotFoundException) {
            errorType = ResourceErrorResponse.ErrorType.NO_SUCH_RESOURCE;
        } else if (t instanceof ResourceAlreadyExistsException) {
            errorType = ResourceErrorResponse.ErrorType.RESOURCE_ALREADY_EXISTS;
        } else if (t instanceof PropertyException) {
            errorType = ResourceErrorResponse.ErrorType.NOT_ACCEPTABLE;
        } else {
            errorType = ResourceErrorResponse.ErrorType.INTERNAL_ERROR;
            level = Logger.Level.WARN;
        }

        log.log(level, errorType, t);

        ctx.writeAndFlush(new DefaultResourceErrorResponse(inReplyTo, errorType, t.getMessage(), t));
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("Internal error: ", cause);
        DefaultFullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.INTERNAL_SERVER_ERROR);
        response.headers().add(HttpHeaders.Names.CONTENT_LENGTH, 0);
    }
}
