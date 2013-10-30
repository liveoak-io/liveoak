package org.projectodd.restafari.container.responders;

import io.netty.channel.ChannelHandlerContext;
import org.projectodd.restafari.container.ResourceErrorResponse;
import org.projectodd.restafari.container.ResourceRequest;
import org.projectodd.restafari.container.ResourceResponse;
import org.projectodd.restafari.spi.RequestContext;
import org.projectodd.restafari.spi.resource.Resource;
import org.projectodd.restafari.spi.resource.async.Responder;

/**
 * @author Bob McWhirter
 */
public class BaseResponder implements Responder {


    public BaseResponder(ResourceRequest inReplyTo, ChannelHandlerContext ctx) {
        this.inReplyTo = inReplyTo;
        this.ctx = ctx;
    }

    BaseResponder createBaseResponder() {
        return new BaseResponder( this.inReplyTo, this.ctx );
    }

    ResourceRequest inReplyTo() {
        return this.inReplyTo;
    }

    @Override
    public void resourceRead(Resource resource) {
        this.ctx.writeAndFlush( new ResourceResponse( this.inReplyTo, ResourceResponse.ResponseType.READ, resource ));
    }

    @Override
    public void resourceCreated(Resource resource) {
        this.ctx.writeAndFlush( new ResourceResponse( this.inReplyTo, ResourceResponse.ResponseType.CREATED, resource ));
    }

    @Override
    public void resourceDeleted(Resource resource) {
        this.ctx.writeAndFlush( new ResourceResponse( this.inReplyTo, ResourceResponse.ResponseType.DELETED, resource ));
    }

    @Override
    public void resourceUpdated(Resource resource) {
        this.ctx.writeAndFlush( new ResourceResponse( this.inReplyTo, ResourceResponse.ResponseType.UPDATED, resource ));
    }

    @Override
    public void createNotSupported(Resource resource) {
        this.ctx.writeAndFlush( new ResourceErrorResponse( this.inReplyTo, ResourceErrorResponse.ErrorType.CREATE_NOT_SUPPORTED) );
    }

    @Override
    public void readNotSupported(Resource resource) {
        this.ctx.writeAndFlush( new ResourceErrorResponse( this.inReplyTo, ResourceErrorResponse.ErrorType.READ_NOT_SUPPORTED) );
    }

    @Override
    public void updateNotSupported(Resource resource) {
        this.ctx.writeAndFlush( new ResourceErrorResponse( this.inReplyTo, ResourceErrorResponse.ErrorType.UPDATE_NOT_SUPPORTED) );
    }

    @Override
    public void deleteNotSupported(Resource resource) {
        this.ctx.writeAndFlush( new ResourceErrorResponse( this.inReplyTo, ResourceErrorResponse.ErrorType.DELETE_NOT_SUPPORTED) );
    }

    @Override
    public void noSuchResource(String id) {
        this.ctx.writeAndFlush( new ResourceErrorResponse( this.inReplyTo, ResourceErrorResponse.ErrorType.NO_SUCH_RESOURCE) );
    }

    @Override
    public void internalError(String message) {
        this.ctx.writeAndFlush( new ResourceErrorResponse( this.inReplyTo, ResourceErrorResponse.ErrorType.INTERNAL_ERROR) );
    }

    @Override
    public RequestContext requestContext() {
        return inReplyTo.requestContext();
    }

    private final ResourceRequest inReplyTo;
    private final ChannelHandlerContext ctx;
}
