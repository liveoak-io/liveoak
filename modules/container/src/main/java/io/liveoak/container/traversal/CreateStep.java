package io.liveoak.container.traversal;

import io.liveoak.spi.resource.async.BinaryResource;
import io.liveoak.spi.resource.async.Resource;
import io.liveoak.spi.resource.async.Responder;

/**
* @author Bob McWhirter
*/
public class CreateStep implements TraversalPlan.Step {

    private boolean complete;

    @Override
    public void execute(TraversalPlan.StepContext context, Resource resource) throws Exception {

        if (complete) {
            if (resource instanceof BinaryResource) {
                ((BinaryResource) resource)
                        .updateContent(context.requestContext(), context.state(), context.responder());
            } else {
                resource.createMember(context.requestContext(), context.state(), context.responder());
            }
        } else {
            if (resource instanceof BinaryResource) {
                BinaryResource binResource = (BinaryResource) resource;
                if (binResource.willProcessUpdate(context.requestContext(), context.state(), context.responder())) {
                    arrangeCompletion(context);
                }
            } else {
                if (!arrangeCompletion(context)) {
                    resource.createMember(context.requestContext(), context.state(), context.responder());
                }
            }
        }
    }

    protected boolean arrangeCompletion(TraversalPlan.StepContext context) {
        Responder responder = context.responder();
        if (responder instanceof TraversingResponder == false) {
            throw new IllegalStateException("Create operation only works within the context of TraversingResponder!");
        }
        TraversingResponder tr = (TraversingResponder) responder;
        if (tr.canContinue()) {
            complete = true;
            tr.dispatchInvocation(context.invocation());
            return true;
        }

        return false;
    }
}
