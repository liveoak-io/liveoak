package io.liveoak.container.traversal;

import io.liveoak.spi.resource.ConfigurableResource;
import io.liveoak.spi.resource.async.Resource;

/**
* @author Bob McWhirter
*/
public class ConfigurationStep implements TraversalPlan.Step {

    @Override
    public void execute(TraversalPlan.StepContext context, Resource resource) {
        if (resource instanceof ConfigurableResource) {
            context.responder().resourceRead(((ConfigurableResource) resource).configuration());
        } else {
            context.responder().noSuchResource(";config");
        }
    }
}
