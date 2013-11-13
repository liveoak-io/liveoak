package io.liveoak.container.responders;

import io.netty.channel.ChannelHandlerContext;
import io.liveoak.container.ResourceRequest;
import io.liveoak.container.aspects.ResourceAspectManager;
import io.liveoak.spi.resource.async.Resource;
import io.liveoak.spi.state.ResourceState;

import java.util.concurrent.Executor;

/**
 * @author Bob McWhirter
 */
public class UpdateResponder extends TraversingResponder {

    public UpdateResponder(ResourceAspectManager aspectManager, Executor executor, Resource root, ResourceRequest inReplyTo, ChannelHandlerContext ctx) {
        super(aspectManager, executor, root, inReplyTo, ctx);
    }

    @Override
    public void perform(Resource resource) {
            resource.updateProperties(inReplyTo().requestContext(), inReplyTo().state(), createBaseResponder());
    }

    @Override
    public void noSuchResource(String id) {
        if (isSeekingTail() ) {
            // Turn it into a Create on its parent, for upsert semantics
            ResourceState state = inReplyTo().state();
            state.id(id);
            currentResource().createMember(inReplyTo().requestContext(), inReplyTo().state(), createBaseResponder());
        } else {
            super.noSuchResource(id);
        }
    }

}
