package org.projectodd.restafari.container.responders;

import io.netty.channel.ChannelHandlerContext;
import org.projectodd.restafari.container.ResourceRequest;
import org.projectodd.restafari.spi.resource.Resource;

import java.util.concurrent.Executor;

/**
 * @author Bob McWhirter
 */
public class ReadResponder extends TraversingResponder {

    public ReadResponder(Executor executor, Resource root, ResourceRequest inReplyTo, ChannelHandlerContext ctx) {
        super( executor, root, inReplyTo, ctx );
    }

    @Override
    protected void perform(Resource resource) {
        createBaseResponder().resourceRead(resource);
    }

}
