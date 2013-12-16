package io.liveoak.container.traversal;

import io.liveoak.spi.resource.async.Resource;

/**
* @author Bob McWhirter
*/
public class ConfigurationStep implements TraversalPlan.Step {

    @Override
    public void execute(TraversalPlan.StepContext context, Resource resource) {
        Resource configurationResource = resource.configuration();
        if (configurationResource != null) {
            context.responder().resourceRead(configurationResource);
        } else {
            context.responder().invalidRequest("No configuration option available on resource at " + resource.uri());
        }
    }
}
