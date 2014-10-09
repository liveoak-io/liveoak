/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.container.traversal;

import io.liveoak.common.DefaultResourceErrorResponse;
import io.liveoak.common.DefaultResourceResponse;
import io.liveoak.container.protocols.http.HttpRequestBodyHandler;
import io.liveoak.spi.ResourceErrorResponse;
import io.liveoak.spi.ResourceRequest;
import io.liveoak.spi.ResourceResponse;
import io.liveoak.spi.resource.async.Resource;
import io.liveoak.spi.resource.async.Responder;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import org.jboss.logging.Logger;

/**
 * @author Bob McWhirter
 */
public class BaseResponder implements Responder {

    private static String RESOURCE_READ_DECODER = "http-resource-decoder";

    private static Logger log = Logger.getLogger(BaseResponder.class);

    public BaseResponder(ResourceRequest inReplyTo, ChannelHandlerContext ctx) {
        this.inReplyTo = inReplyTo;
        this.ctx = ctx;
    }

    BaseResponder createBaseResponder() {
        return new BaseResponder(this.inReplyTo, this.ctx);
    }

    ResourceRequest inReplyTo() {
        return this.inReplyTo;
    }

    @Override
    public void resourceRead(Resource resource) {
        this.ctx.writeAndFlush(new DefaultResourceResponse(this.inReplyTo, ResourceResponse.ResponseType.READ, resource));
        resumeRead();
    }

    @Override
    public void resourceCreated(Resource resource) {
        this.ctx.writeAndFlush(new DefaultResourceResponse(this.inReplyTo, ResourceResponse.ResponseType.CREATED, resource));
        resumeRead();
    }

    @Override
    public void resourceDeleted(Resource resource) {
        this.ctx.writeAndFlush(new DefaultResourceResponse(this.inReplyTo, ResourceResponse.ResponseType.DELETED, resource));
        resumeRead();
    }

    @Override
    public void resourceUpdated(Resource resource) {
        this.ctx.writeAndFlush(new DefaultResourceResponse(this.inReplyTo, ResourceResponse.ResponseType.UPDATED, resource));
        resumeRead();
    }

    @Override
    public void createNotSupported(Resource resource) {
        this.ctx.writeAndFlush(new DefaultResourceErrorResponse(this.inReplyTo, ResourceErrorResponse.ErrorType.CREATE_NOT_SUPPORTED));
        resumeRead();
    }

    @Override
    public void readNotSupported(Resource resource) {
        this.ctx.writeAndFlush(new DefaultResourceErrorResponse(this.inReplyTo, ResourceErrorResponse.ErrorType.READ_NOT_SUPPORTED));
        resumeRead();
    }

    @Override
    public void updateNotSupported(Resource resource) {
        this.ctx.writeAndFlush(new DefaultResourceErrorResponse(this.inReplyTo, ResourceErrorResponse.ErrorType.UPDATE_NOT_SUPPORTED));
        resumeRead();
    }

    @Override
    public void deleteNotSupported(Resource resource) {
        this.ctx.writeAndFlush(new DefaultResourceErrorResponse(this.inReplyTo, ResourceErrorResponse.ErrorType.DELETE_NOT_SUPPORTED));
        resumeRead();
    }

    @Override
    public void noSuchResource(String id) {
        this.ctx.writeAndFlush(new DefaultResourceErrorResponse(this.inReplyTo, ResourceErrorResponse.ErrorType.NO_SUCH_RESOURCE));
        resumeRead();
    }

    @Override
    public void resourceAlreadyExists(String id) {
        this.ctx.writeAndFlush(new DefaultResourceErrorResponse(this.inReplyTo, ResourceErrorResponse.ErrorType.RESOURCE_ALREADY_EXISTS));
        resumeRead();
    }

    @Override
    public void internalError(String message) {
        log.error(message, new RuntimeException("Stack trace: "));
        this.ctx.writeAndFlush(new DefaultResourceErrorResponse(this.inReplyTo, ResourceErrorResponse.ErrorType.INTERNAL_ERROR, message));
        resumeRead();
    }

    @Override
    public void internalError(String message, Throwable cause) {
        log.error(message, cause);
        this.ctx.writeAndFlush(new DefaultResourceErrorResponse(this.inReplyTo, ResourceErrorResponse.ErrorType.INTERNAL_ERROR, message, cause));
        resumeRead();
    }

    @Override
    public void internalError(Throwable cause) {
        log.error("Internal error: ", cause);
        this.ctx.writeAndFlush(new DefaultResourceErrorResponse(this.inReplyTo, ResourceErrorResponse.ErrorType.INTERNAL_ERROR, cause));
        resumeRead();
    }

    @Override
    public void invalidRequest(String message) {
        log.debug(message, new RuntimeException("Stack trace: "));
        this.ctx.writeAndFlush(new DefaultResourceErrorResponse(this.inReplyTo, ResourceErrorResponse.ErrorType.NOT_ACCEPTABLE, message));
        resumeRead();
    }

    @Override
    public void invalidRequest(Throwable cause) {
        log.debug("Invalid request: ", cause);
        this.ctx.writeAndFlush(new DefaultResourceErrorResponse(this.inReplyTo, ResourceErrorResponse.ErrorType.NOT_ACCEPTABLE, cause));
        resumeRead();
    }

    @Override
    public void invalidRequest(String message, Throwable cause) {
        log.debug(message, cause);
        this.ctx.writeAndFlush(new DefaultResourceErrorResponse(this.inReplyTo, ResourceErrorResponse.ErrorType.NOT_ACCEPTABLE, cause));
        resumeRead();
    }

    @Override
    public void error(ResourceErrorResponse.ErrorType errorType) {
        log.debug("error(): " + errorType);
        this.ctx.writeAndFlush(new DefaultResourceErrorResponse(this.inReplyTo, errorType));
        resumeRead();
    }

    @Override
    public void error(ResourceErrorResponse.ErrorType errorType, String message) {
        log.debug("error(): " + errorType + ", message: " + message);
        this.ctx.writeAndFlush(new DefaultResourceErrorResponse(this.inReplyTo, errorType, message));
        resumeRead();
    }

    @Override
    public void error(ResourceErrorResponse.ErrorType errorType, String message, Throwable cause) {
        log.debug("[IGNORED] error(): " + errorType + ", message: " + message + ", cause: ", cause);
        this.ctx.writeAndFlush(new DefaultResourceErrorResponse(this.inReplyTo, errorType, message, cause));
        resumeRead();
    }

    protected boolean canContinue() {
        ChannelHandlerContext context = this.ctx.pipeline().context(RESOURCE_READ_DECODER);
        return context != null;
    }

    protected void dispatchInvocation(Runnable invocation) {
        ChannelHandlerContext context = this.ctx.pipeline().context(RESOURCE_READ_DECODER);
        HttpRequestBodyHandler.Invocation completion = new HttpRequestBodyHandler.Invocation(invocation);
        context.fireChannelRead(completion);
        // signal we're ready to read some more.
        context.read();
    }

    protected void resumeRead() {
        ChannelPipeline pipeline = ctx.pipeline();
        if (pipeline == null) {
            return;
        }
        ChannelHandlerContext context = pipeline.firstContext();
        if (context == null) {
            return;
        }
        context.read();
    }

    private final ResourceRequest inReplyTo;
    private final ChannelHandlerContext ctx;
}
