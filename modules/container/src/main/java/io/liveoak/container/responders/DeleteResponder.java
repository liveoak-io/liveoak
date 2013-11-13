package io.liveoak.container.responders;

import io.netty.channel.ChannelHandlerContext;
import io.liveoak.container.ResourceRequest;
import io.liveoak.container.aspects.ResourceAspectManager;
import io.liveoak.spi.resource.async.Resource;

import java.util.concurrent.Executor;

/**
 * @author Bob McWhirter
 */
public class DeleteResponder extends TraversingResponder {

    public DeleteResponder(ResourceAspectManager aspectManager, Executor executor, Resource root, ResourceRequest inReplyTo, ChannelHandlerContext ctx) {
        super( aspectManager, executor, root, inReplyTo, ctx );
    }

    @Override
    public void perform(Resource resource) {
        resource.delete( inReplyTo().requestContext(), createBaseResponder() );
    }

}
