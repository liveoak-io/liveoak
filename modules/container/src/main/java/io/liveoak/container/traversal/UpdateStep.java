package io.liveoak.container.traversal;

import io.liveoak.spi.resource.async.BinaryResource;
import io.liveoak.spi.resource.async.DelegatingResponder;
import io.liveoak.spi.resource.async.Resource;
import io.liveoak.spi.resource.async.Responder;
import io.liveoak.spi.state.BinaryResourceState;

/**
 * @author Bob McWhirter
 */
public class UpdateStep implements TraversalPlan.Step {
    @Override
    public void execute(TraversalPlan.StepContext context, Resource resource) throws Exception {
        if (resource instanceof BinaryResource) {
            ((BinaryResource) resource).updateContent(context.requestContext(), (BinaryResourceState) context.state(), context.responder());
        } else {
            resource.updateProperties(context.requestContext(), context.state(), context.responder());
        }
    }

    @Override
    public Responder createResponder(TraversingResponder responder) {
        return new DelegatingResponder(responder) {
            @Override
            public void noSuchResource(String id) {
                responder.inReplyTo().state().id( id );
                responder.replaceStep( UpdateStep.this, new CreateStep() );
                responder.doNextStep( responder.currentResource() );
            }
        };
    }
}
