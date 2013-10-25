package org.projectodd.restafari.container.responders;

import io.netty.channel.ChannelHandlerContext;
import org.projectodd.restafari.container.ResourceRequest;
import org.projectodd.restafari.spi.resource.Resource;

import java.util.concurrent.Executor;

/**
 * @author Bob McWhirter
 */
public class DeleteResponder extends TraversingResponder {

    public DeleteResponder(Executor executor, Resource root, ResourceRequest inReplyTo, ChannelHandlerContext ctx) {
        super( executor, root, inReplyTo, ctx );
    }

    @Override
    public void perform(Resource resource) {
        resource.delete( createBaseResponder() );
    }

}
