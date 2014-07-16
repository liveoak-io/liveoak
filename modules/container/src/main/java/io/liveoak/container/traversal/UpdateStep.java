package io.liveoak.container.traversal;

import io.liveoak.spi.resource.async.BinaryResource;
import io.liveoak.spi.resource.async.DelegatingResponder;
import io.liveoak.spi.resource.async.Resource;
import io.liveoak.spi.resource.async.Responder;

/**
 * @author Bob McWhirter
 */
public class UpdateStep implements TraversalPlan.Step {

    private boolean complete;

    @Override
    public void execute(TraversalPlan.StepContext context, Resource resource) throws Exception {
        if (complete) {
            if (resource instanceof BinaryResource) {
                ((BinaryResource) resource)
                        .updateContent(context.requestContext(), context.state(), context.responder());
            } else {
                resource.updateProperties(context.requestContext(), context.state(), context.responder());
            }
        } else {
            if (resource instanceof BinaryResource) {
                BinaryResource binResource = (BinaryResource) resource;
                if (binResource.willProcessUpdate(context.requestContext(), context.state(), context.responder())) {
                    arrangeCompletion(context);
                }
            } else {
                if (!arrangeCompletion(context)) {
                    resource.updateProperties(context.requestContext(), context.state(), context.responder());
                }
            }
        }
    }

    protected boolean arrangeCompletion(TraversalPlan.StepContext context) {
        Responder responder = context.responder();
        if (responder instanceof TraversingResponder == false) {
            throw new IllegalStateException("Update operation only works within the context of TraversingResponder!");
        }
        TraversingResponder tr = (TraversingResponder) responder;
        if (tr.canContinue()) {
            complete = true;
            tr.dispatchInvocation(context.invocation());
            return true;
        }

        return false;
    }

    @Override
    public Responder createResponder(TraversingResponder responder) {
        return new DelegatingResponder(responder) {
            @Override
            public void noSuchResource(String id) {
                responder.inReplyTo().state().id(id);
                responder.replaceStep(UpdateStep.this, new CreateStep());
                responder.doNextStep(responder.currentResource());
            }
        };
    }
}
