package org.projectodd.restafari.container;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.HttpMethod;
import org.projectodd.restafari.container.codec.ResourceCodec;
import org.projectodd.restafari.container.requests.ResourceRequest;
import org.projectodd.restafari.spi.Resource;
import org.projectodd.restafari.spi.ResourceController;
import org.projectodd.restafari.spi.Responder;

import java.io.IOException;
import java.util.function.Consumer;

public class ResourceHandler extends SimpleChannelInboundHandler<ResourceRequest> {

    public ResourceHandler(Container container) {
        this.container = container;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ResourceRequest msg) throws Exception {
        final Responder responder = new ResponderImpl(this.container, msg, ctx );

        Holder holder = container.getResourceController(msg.type());

        if( holder == null ) {
            responder.noSuchCollection( msg.type() );
            return;
        }

        ResourceController controller = holder.getResourceController();

        switch ( msg.requestType() ) {
            case CREATE:
                controller.createResource( null, msg.collectionName(), msg.resource(), responder );
                break;
            case READ:
                controller.getResource( null, msg.collectionName(), msg.resourceId(), responder );
                break;
            case UPDATE:
                controller.updateResource(null, msg.collectionName(), msg.resourceId(), msg.resource(), responder );
                break;
            case DELETE:
                controller.deleteResource( null, msg.collectionName(), msg.resourceId(), responder);
                break;
        }
    }

    private Container container;

}
