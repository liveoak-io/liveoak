package org.projectodd.restafari.container.responders;

import io.netty.channel.ChannelHandlerContext;
import org.projectodd.restafari.container.ResourceRequest;
import org.projectodd.restafari.container.aspects.ResourceAspectManager;
import org.projectodd.restafari.spi.resource.async.Resource;
import org.projectodd.restafari.spi.state.ResourceState;

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
