/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.container.traversal;

import io.liveoak.container.ResourceErrorResponse;
import io.liveoak.container.ResourceRequest;
import io.liveoak.container.ResourceResponse;
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
        this.ctx.writeAndFlush(new ResourceResponse(this.inReplyTo, ResourceResponse.ResponseType.READ, resource));
    }

    @Override
    public void resourceCreated(Resource resource) {
        this.ctx.writeAndFlush(new ResourceResponse(this.inReplyTo, ResourceResponse.ResponseType.CREATED, resource));
    }

    @Override
    public void resourceDeleted(Resource resource) {
        this.ctx.writeAndFlush(new ResourceResponse(this.inReplyTo, ResourceResponse.ResponseType.DELETED, resource));
    }

    @Override
    public void resourceUpdated(Resource resource) {
        this.ctx.writeAndFlush(new ResourceResponse(this.inReplyTo, ResourceResponse.ResponseType.UPDATED, resource));
    }

    @Override
    public void createNotSupported(Resource resource) {
        this.ctx.writeAndFlush(new ResourceErrorResponse(this.inReplyTo, ResourceErrorResponse.ErrorType.CREATE_NOT_SUPPORTED));
    }

    @Override
    public void readNotSupported(Resource resource) {
        this.ctx.writeAndFlush(new ResourceErrorResponse(this.inReplyTo, ResourceErrorResponse.ErrorType.READ_NOT_SUPPORTED));
    }

    @Override
    public void updateNotSupported(Resource resource) {
        this.ctx.writeAndFlush(new ResourceErrorResponse(this.inReplyTo, ResourceErrorResponse.ErrorType.UPDATE_NOT_SUPPORTED));
    }

    @Override
    public void deleteNotSupported(Resource resource) {
        this.ctx.writeAndFlush(new ResourceErrorResponse(this.inReplyTo, ResourceErrorResponse.ErrorType.DELETE_NOT_SUPPORTED));
    }

    @Override
    public void noSuchResource(String id) {
        this.ctx.writeAndFlush(new ResourceErrorResponse(this.inReplyTo, ResourceErrorResponse.ErrorType.NO_SUCH_RESOURCE));
    }

    @Override
    public void resourceAlreadyExists(String id) {
        this.ctx.writeAndFlush(new ResourceErrorResponse(this.inReplyTo, ResourceErrorResponse.ErrorType.RESOURCE_ALREADY_EXISTS));
    }

    @Override
    public void internalError(String message) {
        this.ctx.writeAndFlush(new ResourceErrorResponse(this.inReplyTo, ResourceErrorResponse.ErrorType.INTERNAL_ERROR, message ));
    }

    @Override
    public void internalError(Throwable cause) {
        this.ctx.writeAndFlush(new ResourceErrorResponse(this.inReplyTo, ResourceErrorResponse.ErrorType.INTERNAL_ERROR, cause));
    }

    @Override
    public void invalidRequest(String message) {
        this.ctx.writeAndFlush(new ResourceErrorResponse(this.inReplyTo, ResourceErrorResponse.ErrorType.NOT_ACCEPTABLE, message));
    }

    private final ResourceRequest inReplyTo;
    private final ChannelHandlerContext ctx;
}
