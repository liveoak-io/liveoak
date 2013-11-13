package io.liveoak.container.responders;

import io.netty.channel.ChannelHandlerContext;
import io.liveoak.container.ResourceRequest;
import io.liveoak.container.aspects.ResourceAspectManager;
import io.liveoak.spi.resource.async.Resource;

import java.util.concurrent.Executor;

/**
 * @author Bob McWhirter
 */
public class ReadResponder extends TraversingResponder {

    public ReadResponder(ResourceAspectManager aspectManager, Executor executor, Resource root, ResourceRequest inReplyTo, ChannelHandlerContext ctx) {
        super( aspectManager, executor, root, inReplyTo, ctx );
    }

    @Override
    protected void perform(Resource resource) {
        createBaseResponder().resourceRead(resource);
    }

}
