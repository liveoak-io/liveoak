package org.projectodd.restafari.container;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpHeaders;
import org.projectodd.restafari.container.codec.ResourceCodec;
import org.projectodd.restafari.container.requests.BaseRequest;
import org.projectodd.restafari.container.requests.ResourceRequest;
import org.projectodd.restafari.container.responses.CollectionResponse;
import org.projectodd.restafari.container.responses.ErrorResponse;
import org.projectodd.restafari.container.responses.ResourceResponse;
import org.projectodd.restafari.container.subscriptions.SubscriptionManager;
import org.projectodd.restafari.spi.Resource;
import org.projectodd.restafari.spi.Responder;

import java.io.IOException;
import java.util.Collection;
import java.util.function.Consumer;

public class ResponderImpl implements Responder {

    public ResponderImpl(Container container, BaseRequest request, ChannelHandlerContext ctx) {
        this.request = request;
        this.ctx = ctx;
    }

    @Override
    public void resource(Resource resource) {
        this.ctx.writeAndFlush(new ResourceResponse(this.request, ResourceResponse.ResponseType.READ, resource));
    }

    @Override
    public void resources(Collection<Resource> resources) {
        this.ctx.writeAndFlush(new CollectionResponse(this.request, CollectionResponse.ResponseType.READ, resources));
    }

    @Override
    public void resourceCreated(Resource resource) {
        this.ctx.writeAndFlush(new ResourceResponse(this.request, ResourceResponse.ResponseType.CREATED, resource));
    }

    @Override
    public void resourceUpdated(Resource resource) {
        this.ctx.writeAndFlush(new ResourceResponse(this.request, ResourceResponse.ResponseType.UPDATED, resource));
    }

    @Override
    public void resourceDeleted(Resource resource) {
        this.ctx.writeAndFlush(new ResourceResponse(this.request, ResourceResponse.ResponseType.DELETED, resource));
    }

    @Override
    public void noSuchCollection(String name) {
        this.ctx.writeAndFlush(new ErrorResponse(this.request, ErrorResponse.ResponseType.NOT_FOUND, name));
    }

    @Override
    public void noSuchResource(String id) {
        this.ctx.writeAndFlush(new ErrorResponse(this.request, ErrorResponse.ResponseType.NOT_FOUND, id));
    }

    @Override
    public void internalError(String message) {
        this.ctx.writeAndFlush(new ErrorResponse(this.request, ErrorResponse.ResponseType.INTERNAL_ERROR, message));
    }

    private BaseRequest request;
    private ChannelHandlerContext ctx;

}
