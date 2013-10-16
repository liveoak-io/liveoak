package org.projectodd.restafari.container.responders;

import io.netty.channel.ChannelHandlerContext;
import org.projectodd.restafari.container.ResourceRequest;
import org.projectodd.restafari.spi.Resource;

/**
 * @author Bob McWhirter
 */
public class ReadResponder extends TraversingResponder {

    public ReadResponder(ResourceRequest inReplyTo, ChannelHandlerContext ctx) {
        super( inReplyTo, ctx );
    }

    @Override
    protected void perform(Resource resource) {
        createBaseResponder().resourceRead(resource);
    }

}
