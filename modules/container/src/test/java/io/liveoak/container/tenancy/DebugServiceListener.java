package io.liveoak.container.tenancy;

import org.jboss.logging.Logger;
import org.jboss.msc.service.ServiceController;
import org.jboss.msc.service.ServiceListener;

/**
 * @author Bob McWhirter
 */
public class DebugServiceListener implements ServiceListener {
    @Override
    public void listenerAdded(ServiceController controller) {

    }

    @Override
    public void transition(ServiceController controller, ServiceController.Transition transition) {
        switch ( transition.getAfter() ) {
            case PROBLEM:
            case START_FAILED:
                log.error(controller.getName() + " start failed.", controller.getStartException());
                break;
        }
    }

    @Override
    public void serviceRemoveRequested(ServiceController controller) {

    }

    @Override
    public void serviceRemoveRequestCleared(ServiceController controller) {

    }

    @Override
    public void dependencyFailed(ServiceController controller) {

    }

    @Override
    public void dependencyFailureCleared(ServiceController controller) {

    }

    @Override
    public void immediateDependencyUnavailable(ServiceController controller) {

    }

    @Override
    public void immediateDependencyAvailable(ServiceController controller) {

    }

    @Override
    public void transitiveDependencyUnavailable(ServiceController controller) {

    }

    @Override
    public void transitiveDependencyAvailable(ServiceController controller) {

    }

    private static final Logger log = Logger.getLogger(DebugServiceListener.class);
}
