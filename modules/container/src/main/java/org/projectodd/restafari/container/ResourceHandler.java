package org.projectodd.restafari.container;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.projectodd.restafari.container.responders.CreateResponder;
import org.projectodd.restafari.container.responders.DeleteResponder;
import org.projectodd.restafari.container.responders.ReadResponder;
import org.projectodd.restafari.container.responders.UpdateResponder;

public class ResourceHandler extends SimpleChannelInboundHandler<ResourceRequest> {

    public ResourceHandler(DefaultContainer container) {
        this.container = container;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ResourceRequest msg) throws Exception {
        String firstSegment = msg.resourcePath().head();
        System.err.println( "request: " + msg.requestType() );
        System.err.println( " -> " + firstSegment );

        switch (msg.requestType()) {
            case CREATE:
                //this.container.readMember(msg.requestContext(), firstSegment, new CreateResponder(container.resourceAspectManager(), container.workerPool(), container, msg, ctx));
                new CreateResponder(container.resourceAspectManager(), container.workerPool(), container, msg, ctx).doRead( firstSegment, container );
                break;
            case READ:
                //this.container.readMember(msg.requestContext(), firstSegment, new ReadResponder(container.resourceAspectManager(), container.workerPool(), container, msg, ctx));
                new ReadResponder(container.resourceAspectManager(), container.workerPool(), container, msg, ctx).doRead( firstSegment, container );
                break;
            case UPDATE:
                //this.container.readMember(msg.requestContext(), firstSegment, new UpdateResponder(container.resourceAspectManager(), container.workerPool(), container, msg, ctx));
                new UpdateResponder(container.resourceAspectManager(), container.workerPool(), container, msg, ctx).doRead( firstSegment, container );
                break;
            case DELETE:
                //this.container.readMember(msg.requestContext(), firstSegment, new DeleteResponder(container.resourceAspectManager(), container.workerPool(), container, msg, ctx));
                new DeleteResponder(container.resourceAspectManager(), container.workerPool(), container, msg, ctx).doRead( firstSegment, container );
                break;
        }
    }

    private DefaultContainer container;

}
