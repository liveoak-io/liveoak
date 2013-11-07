package org.projectodd.restafari.container.responders;

import io.netty.channel.ChannelHandlerContext;
import org.projectodd.restafari.container.ResourceRequest;
import org.projectodd.restafari.container.aspects.ResourceAspectManager;
import org.projectodd.restafari.spi.resource.Resource;

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
