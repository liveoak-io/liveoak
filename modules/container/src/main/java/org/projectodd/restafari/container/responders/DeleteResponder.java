package org.projectodd.restafari.container.responders;

import io.netty.channel.ChannelHandlerContext;
import org.projectodd.restafari.container.ResourceRequest;
import org.projectodd.restafari.spi.Resource;

/**
 * @author Bob McWhirter
 */
public class DeleteResponder extends TraversingResponder {

    public DeleteResponder(ResourceRequest inReplyTo, ChannelHandlerContext ctx) {
        super( inReplyTo, ctx );
    }

    @Override
    public void perform(Resource resource) {
        resource.delete( createBaseResponder() );
    }

}
