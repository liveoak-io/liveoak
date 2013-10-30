package org.projectodd.restafari.container.responders;

import io.netty.channel.ChannelHandlerContext;
import org.projectodd.restafari.container.ResourceRequest;
import org.projectodd.restafari.spi.resource.async.CollectionResource;
import org.projectodd.restafari.spi.resource.async.ObjectResource;
import org.projectodd.restafari.spi.state.ObjectResourceState;
import org.projectodd.restafari.spi.resource.Resource;
import org.projectodd.restafari.spi.state.ResourceState;

import java.util.concurrent.Executor;

/**
 * @author Bob McWhirter
 */
public class UpdateResponder extends TraversingResponder {

    public UpdateResponder(Executor executor, Resource root, ResourceRequest inReplyTo, ChannelHandlerContext ctx) {
        super(executor, root, inReplyTo, ctx);
    }



    @Override
    public void perform(Resource resource) {
        if (resource instanceof ObjectResource) {
            ((ObjectResource) resource).update(inReplyTo().requestContext(), (ObjectResourceState) inReplyTo().state(), createBaseResponder());
        } else {
            updateNotSupported(resource);
        }
    }

    @Override
    public void noSuchResource(String id) {
        if (isSeekingTail() && currentResource() instanceof CollectionResource) {
            // Turn it into a Create on its parent, for upsert semantics
            ResourceState state = inReplyTo().state();
            state.id( id );
            ((CollectionResource) currentResource() ).create(inReplyTo().requestContext(), inReplyTo().state(), createBaseResponder());
        } else {
            super.noSuchResource(id);
        }
    }

}
