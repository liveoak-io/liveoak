package io.liveoak.container.traversal;

import io.liveoak.spi.resource.async.Resource;

/**
* @author Bob McWhirter
*/
public class DeleteStep implements TraversalPlan.Step {

    @Override
    public void execute(TraversalPlan.StepContext context, Resource resource) throws Exception {
        /*
        if (resource instanceof RootResource && resource.parent() == null ) {
            context.container().unregisterResource((RootResource) resource);
            context.responder().resourceDeleted( resource );
        } else {
        */
            resource.delete(context.requestContext(), context.responder());
        //}

    }
}
