package org.projectodd.restafari.container;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;

import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import org.projectodd.restafari.container.requests.*;
import org.projectodd.restafari.container.responses.ErrorResponse;
import org.projectodd.restafari.container.responses.NoSuchCollectionResponse;
import org.projectodd.restafari.spi.Responder;
import org.projectodd.restafari.spi.Resource;

public class ContainerHandler extends ChannelDuplexHandler {

    public ContainerHandler(Container container) {
        this.container = container;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        System.err.println( "container: " + msg );
        if (msg instanceof GetCollectionRequest) {
            dispatchGetCollectionRequest( ctx, (GetCollectionRequest) msg );
        } else if ( msg instanceof GetResourceRequest ) {
            dispatchGetResourceRequest( ctx, (GetResourceRequest) msg );
        } else if (msg instanceof CreateResourceRequest) {
            dispatchCreateResourceRequest(ctx, (CreateResourceRequest) msg);
        } else if (msg instanceof UpdateResourceRequest) {
            dispatchUpdateResourceRequest(ctx, (UpdateResourceRequest) msg);
        } else if (msg instanceof DeleteResourceRequest) {
            dispatchDeleteResourceRequest(ctx, (DeleteResourceRequest) msg);
        } else {
            dispatchNotFound(ctx, msg);
        }
    }

    protected void dispatchNotFound(ChannelHandlerContext ctx, Object msg) {
        DefaultHttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_1,
            new HttpResponseStatus(HttpResponseStatus.NOT_FOUND.code(), HttpResponseStatus.NOT_FOUND.reasonPhrase()));

        response.headers().add(HttpHeaders.Names.CONTENT_LENGTH, 0);
        ctx.pipeline().write(response);
        ctx.pipeline().flush();
    }

    protected void dispatchGetCollectionRequest(ChannelHandlerContext ctx, GetCollectionRequest msg) {
        Holder holder = this.container.getResourceController(msg.getType());
        if (holder == null) {
            ctx.pipeline().write(new NoSuchCollectionResponse(msg.getMimeType(), msg.getType()));
            ctx.pipeline().flush();
        } else {
            holder.getResourceController().getResources(null, msg.getCollectionName(), msg, createResponder(msg, ctx ) );
        }
    }
    
    protected void dispatchGetResourceRequest(ChannelHandlerContext ctx, GetResourceRequest msg) {
        Holder holder = this.container.getResourceController(msg.getType());
        if (holder == null) {
            ctx.pipeline().write(new NoSuchCollectionResponse(msg.getMimeType(), msg.getType()));
            ctx.pipeline().flush();
        } else {
            holder.getResourceController().getResource(null, msg.getCollectionName(), msg.getResourceId(), createResponder(msg, ctx) );
        }
        
    }

    private void dispatchUpdateResourceRequest(ChannelHandlerContext ctx, UpdateResourceRequest msg) throws Exception {
        Holder holder = this.container.getResourceController(msg.getType());
        if (holder == null) {
            ctx.pipeline().write(new NoSuchCollectionResponse(msg.getMimeType(), msg.getType()));
            ctx.pipeline().flush();
        } else {
            Resource resource = (Resource) container.getCodecManager().getResourceCodec(msg.getMimeType()).decode(msg.getContent());
            holder.getResourceController().updateResource(null, msg.getCollectionName(), msg.getResourceId(), resource, createResponder(msg, ctx));
        }
    }

    private void dispatchCreateResourceRequest(ChannelHandlerContext ctx, CreateResourceRequest msg) throws Exception {
        Holder holder = this.container.getResourceController(msg.getType());
        if (holder == null) {
            ctx.pipeline().write(new NoSuchCollectionResponse(msg.getMimeType(), msg.getType()));
            ctx.pipeline().flush();
        } else {
            Resource resource = (Resource) container.getCodecManager().getResourceCodec(msg.getMimeType()).decode(msg.getContent());
            holder.getResourceController().createResource(null, msg.getCollectionName(), resource, createResponder(msg, ctx));
        }
    }

    protected void dispatchDeleteResourceRequest(ChannelHandlerContext ctx, DeleteResourceRequest msg) {
        Holder holder = this.container.getResourceController(msg.getType());
        if (holder == null) {
            ctx.pipeline().write(new NoSuchCollectionResponse(msg.getMimeType(), msg.getType()));
            ctx.pipeline().flush();
        } else {
            holder.getResourceController().deleteResource(null, msg.getCollectionName(), msg.getResourceId(), createResponder(msg, ctx));
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace(System.err);
        ctx.pipeline().write(new ErrorResponse(cause.getMessage()));
        ctx.pipeline().flush();
    }

    protected Responder createResponder(BaseCollectionRequest request, ChannelHandlerContext ctx) {
        return this.container.createResponder( request.getType(), request.getCollectionName(), request.getMimeType(), ctx );
    }


    private Container container;
}
