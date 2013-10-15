package org.projectodd.restafari.container;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.HttpMethod;
import org.projectodd.restafari.container.codec.ResourceCodec;
import org.projectodd.restafari.container.requests.CollectionRequest;
import org.projectodd.restafari.container.requests.ResourceRequest;
import org.projectodd.restafari.spi.Resource;
import org.projectodd.restafari.spi.ResourceController;
import org.projectodd.restafari.spi.Responder;

import java.io.IOException;
import java.util.function.Consumer;

public class CollectionHandler extends SimpleChannelInboundHandler<CollectionRequest> {

    public CollectionHandler(Container container) {
        this.container = container;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, CollectionRequest msg) throws Exception {
        final Responder responder = new ResponderImpl(this.container, msg, ctx );

        Holder holder = container.getResourceController(msg.type());

        if( holder == null ) {
            responder.noSuchCollection( msg.type() );
            return;
        }


        switch ( msg.requestType() ) {
            case READ:
                holder.getResourceController().getResources(null, msg.collectionName(), msg.pagination(), responder);
                break;
        }
    }

    private Container container;

}
