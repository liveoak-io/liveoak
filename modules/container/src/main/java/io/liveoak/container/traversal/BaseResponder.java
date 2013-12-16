/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.container.traversal;

import io.liveoak.common.DefaultResourceErrorResponse;
import io.liveoak.common.DefaultResourceResponse;
import io.liveoak.spi.ResourceErrorResponse;
import io.liveoak.spi.ResourceRequest;
import io.liveoak.spi.resource.async.Resource;
import io.liveoak.spi.resource.async.Responder;
import io.netty.channel.ChannelHandlerContext;

/**
 * @author Bob McWhirter
 */
public class BaseResponder implements Responder {

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
        this.ctx.writeAndFlush(new DefaultResourceResponse(this.inReplyTo, DefaultResourceResponse.ResponseType.READ, resource));
    }

    @Override
    public void resourceCreated(Resource resource) {
        this.ctx.writeAndFlush(new DefaultResourceResponse(this.inReplyTo, DefaultResourceResponse.ResponseType.CREATED, resource));
    }

    @Override
    public void resourceDeleted(Resource resource) {
        this.ctx.writeAndFlush(new DefaultResourceResponse(this.inReplyTo, DefaultResourceResponse.ResponseType.DELETED, resource));
    }

    @Override
    public void resourceUpdated(Resource resource) {
        this.ctx.writeAndFlush(new DefaultResourceResponse(this.inReplyTo, DefaultResourceResponse.ResponseType.UPDATED, resource));
    }

    @Override
    public void createNotSupported(Resource resource) {
        this.ctx.writeAndFlush(new DefaultResourceErrorResponse(this.inReplyTo, DefaultResourceErrorResponse.ErrorType.CREATE_NOT_SUPPORTED));
    }

    @Override
    public void readNotSupported(Resource resource) {
        this.ctx.writeAndFlush(new DefaultResourceErrorResponse(this.inReplyTo, DefaultResourceErrorResponse.ErrorType.READ_NOT_SUPPORTED));
    }

    @Override
    public void updateNotSupported(Resource resource) {
        this.ctx.writeAndFlush(new DefaultResourceErrorResponse(this.inReplyTo, DefaultResourceErrorResponse.ErrorType.UPDATE_NOT_SUPPORTED));
    }

    @Override
    public void deleteNotSupported(Resource resource) {
        this.ctx.writeAndFlush(new DefaultResourceErrorResponse(this.inReplyTo, DefaultResourceErrorResponse.ErrorType.DELETE_NOT_SUPPORTED));
    }

    @Override
    public void noSuchResource(String id) {
        this.ctx.writeAndFlush(new DefaultResourceErrorResponse(this.inReplyTo, DefaultResourceErrorResponse.ErrorType.NO_SUCH_RESOURCE));
    }

    @Override
    public void resourceAlreadyExists(String id) {
        this.ctx.writeAndFlush(new DefaultResourceErrorResponse(this.inReplyTo, DefaultResourceErrorResponse.ErrorType.RESOURCE_ALREADY_EXISTS));
    }

    @Override
    public void internalError(String message) {
        this.ctx.writeAndFlush(new DefaultResourceErrorResponse(this.inReplyTo, DefaultResourceErrorResponse.ErrorType.INTERNAL_ERROR, message ));
    }

    @Override
    public void internalError(Throwable cause) {
        this.ctx.writeAndFlush(new DefaultResourceErrorResponse(this.inReplyTo, DefaultResourceErrorResponse.ErrorType.INTERNAL_ERROR, cause));
    }

    @Override
    public void invalidRequest(String message) {
        this.ctx.writeAndFlush(new DefaultResourceErrorResponse(this.inReplyTo, ResourceErrorResponse.ErrorType.NOT_ACCEPTABLE, message));
    }

    @Override
    public void invalidRequest( Throwable cause ) {
        this.ctx.writeAndFlush(new DefaultResourceErrorResponse(this.inReplyTo, ResourceErrorResponse.ErrorType.NOT_ACCEPTABLE, cause));
    }

    @Override
    public void invalidRequest( String message, Throwable cause ) {
        this.ctx.writeAndFlush(new DefaultResourceErrorResponse(this.inReplyTo, ResourceErrorResponse.ErrorType.NOT_ACCEPTABLE, cause));
    }

    private final ResourceRequest inReplyTo;
    private final ChannelHandlerContext ctx;
}
