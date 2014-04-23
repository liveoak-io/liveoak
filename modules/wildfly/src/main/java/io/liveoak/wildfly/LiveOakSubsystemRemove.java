package io.liveoak.wildfly;

import org.jboss.as.controller.AbstractRemoveStepHandler;
import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.dmr.ModelNode;
import org.jboss.logging.Logger;

/**
 * @author Bob McWhirter
 */
public class LiveOakSubsystemRemove extends AbstractRemoveStepHandler{
    static final LiveOakSubsystemRemove INSTANCE = new LiveOakSubsystemRemove();

    private final Logger log = Logger.getLogger(LiveOakSubsystemRemove.class);

    private LiveOakSubsystemRemove() {
    }

    @Override
    protected void performRuntime(OperationContext context, ModelNode operation, ModelNode model) throws OperationFailedException {
        super.performRuntime(context, operation, model);
        //context.removeService(SimpleService.NAME);
    }
}
