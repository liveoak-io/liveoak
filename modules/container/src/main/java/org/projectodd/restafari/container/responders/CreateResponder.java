package org.projectodd.restafari.container.responders;

import io.netty.channel.ChannelHandlerContext;
import org.projectodd.restafari.container.ResourcePath;
import org.projectodd.restafari.container.ResourceRequest;
import org.projectodd.restafari.spi.CollectionResource;
import org.projectodd.restafari.spi.Resource;

/**
 * @author Bob McWhirter
 */
public class CreateResponder extends TraversingResponder {

    public CreateResponder(ResourceRequest inReplyTo, ChannelHandlerContext ctx) {
        super( inReplyTo, ctx );
    }

    @Override
    public void perform(Resource resource) {
        if ( resource instanceof CollectionResource ) {
            ((CollectionResource) resource).create( inReplyTo().state(), createBaseResponder() );
        } else {
            this.createNotSupported( resource );
        }
    }

}
