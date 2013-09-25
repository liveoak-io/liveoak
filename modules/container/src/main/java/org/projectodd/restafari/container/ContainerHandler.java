package org.projectodd.restafari.container;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;

import org.projectodd.restafari.container.requests.GetCollectionRequest;
import org.projectodd.restafari.container.requests.GetResourceRequest;
import org.projectodd.restafari.container.responses.NoSuchCollectionResponse;

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
        }
    }

    protected void dispatchGetCollectionRequest(ChannelHandlerContext ctx, GetCollectionRequest msg) {
        Holder holder = this.container.getResourceController(msg.getType());
        if (holder == null) {
            ctx.pipeline().write(new NoSuchCollectionResponse(msg.getMimeType(), msg.getType()));
            ctx.pipeline().flush();
        } else {
            holder.getResourceController().getResources(null, msg.getCollectionName(), msg, new ResponderImpl(msg.getMimeType(), ctx));
        }
    }
    
    protected void dispatchGetResourceRequest(ChannelHandlerContext ctx, GetResourceRequest msg) {
        Holder holder = this.container.getResourceController(msg.getType());
        if (holder == null) {
            ctx.pipeline().write(new NoSuchCollectionResponse(msg.getMimeType(), msg.getType()));
            ctx.pipeline().flush();
        } else {
            holder.getResourceController().getResource(null, msg.getCollectionName(), msg.getResourceId(), new ResponderImpl( msg.getMimeType(), ctx ) );
        }
        
    }


    private Container container;
}
