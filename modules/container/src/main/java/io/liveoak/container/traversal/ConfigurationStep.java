package io.liveoak.container.traversal;

import io.liveoak.spi.resource.async.Resource;

/**
* @author Bob McWhirter
*/
public class ConfigurationStep implements TraversalPlan.Step {

    @Override
    public void execute(TraversalPlan.StepContext context, Resource resource) {
        context.responder().resourceRead(resource.configuration());
    }
}
