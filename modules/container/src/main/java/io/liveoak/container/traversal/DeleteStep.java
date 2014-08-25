package io.liveoak.container.traversal;

import io.liveoak.spi.resource.async.Resource;

/**
* @author Bob McWhirter
*/
public class DeleteStep implements TraversalPlan.Step {

    @Override
    public void execute(TraversalPlan.StepContext context, Resource resource) throws Exception {
        resource.delete(context.requestContext(), context.responder());
    }
}
