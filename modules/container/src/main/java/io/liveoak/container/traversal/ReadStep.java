package io.liveoak.container.traversal;

import io.liveoak.spi.resource.async.Resource;

/**
* @author Bob McWhirter
*/
public class ReadStep implements TraversalPlan.Step {
    public ReadStep(String name) {
        this.name = name;
    }

    public String name() {
        return this.name;
    }

    private String name;

    @Override
    public void execute(TraversalPlan.StepContext context, Resource resource) throws Exception {
        resource.readMember(context.requestContext(), this.name, context.responder());
    }

    public String toString() {
        return "[ReadStep: name=" + this.name + "]";
    }
}
