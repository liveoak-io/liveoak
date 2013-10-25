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

        switch (msg.requestType()) {
            case CREATE:
                this.container.read(firstSegment, new CreateResponder(container.workerPool(), container, msg, ctx));
                break;
            case READ:
                this.container.read(firstSegment, new ReadResponder(container.workerPool(), container, msg, ctx));
                break;
            case UPDATE:
                this.container.read(firstSegment, new UpdateResponder(container.workerPool(), container, msg, ctx));
                break;
            case DELETE:
                this.container.read(firstSegment, new DeleteResponder(container.workerPool(), container, msg, ctx));
                break;
        }
    }

    private DefaultContainer container;

}
