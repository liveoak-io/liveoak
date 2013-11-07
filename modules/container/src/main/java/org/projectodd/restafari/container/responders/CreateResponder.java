package org.projectodd.restafari.container.responders;

import io.netty.channel.ChannelHandlerContext;
import org.projectodd.restafari.container.ResourceRequest;
import org.projectodd.restafari.container.aspects.ResourceAspectManager;
import org.projectodd.restafari.spi.resource.async.CollectionResource;
import org.projectodd.restafari.spi.resource.Resource;

import java.util.concurrent.Executor;

/**
 * @author Bob McWhirter
 */
public class CreateResponder extends TraversingResponder {

    public CreateResponder(ResourceAspectManager aspectManager, Executor executor, Resource root, ResourceRequest inReplyTo, ChannelHandlerContext ctx) {
        super( aspectManager, executor, root, inReplyTo, ctx );
    }

    @Override
    public void perform(Resource resource) {
        if ( resource instanceof CollectionResource ) {
            ((CollectionResource) resource).create( inReplyTo().requestContext(), inReplyTo().state(), createBaseResponder() );
        } else {
            this.createNotSupported( resource );
        }
    }

}
