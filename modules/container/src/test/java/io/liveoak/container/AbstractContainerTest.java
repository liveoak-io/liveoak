package io.liveoak.container;

import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.jboss.msc.service.ServiceController;

/**
 * @author Ken Finnigan
 */
public class AbstractContainerTest {

    protected LiveOakSystem system;

    protected boolean awaitStability() throws InterruptedException {
        // Default all calls to a 7 second timeout if not specified
        return awaitStability(7, TimeUnit.SECONDS);
    }

    protected boolean awaitStability(int timeout, TimeUnit unit) throws InterruptedException {
        return awaitStability(timeout, unit, null, null);
    }

    protected boolean awaitStability(long timeout, TimeUnit unit, Set<? super ServiceController<?>> failed, Set<? super ServiceController<?>> problem) throws InterruptedException {
        boolean stable = this.system.awaitStability(timeout, unit, failed, problem);
        if (!stable) {
            System.out.println("awaitStability() may require an increased timeout duration.");
        } else {
            if (failed != null && !failed.isEmpty()) {
                Iterator<? super ServiceController<?>> failedIterator = failed.iterator();
                while (failedIterator.hasNext()) {
                    ServiceController controller = (ServiceController) failedIterator.next();
                    System.err.printf("Controller %s is in State: %s, Substate: %s and Mode: %s", controller.getName(), controller.getState(), controller.getSubstate(), controller.getMode());
                    if (controller.getStartException() != null) {
                        System.err.println(controller.getStartException());
                        controller.getStartException().printStackTrace();
                    }
                }
            }
            if (problem != null) {
                Iterator<? super ServiceController<?>> problemIterator = problem.iterator();
                while (problemIterator.hasNext()) {
                    ServiceController controller = (ServiceController) problemIterator.next();
                    System.err.printf("Controller %s is in State: %s, Substate: %s and Mode: %s", controller.getName(), controller.getState(), controller.getSubstate(), controller.getMode());
                    if (controller.getStartException() != null) {
                        System.err.println(controller.getStartException());
                        controller.getStartException().printStackTrace();
                    }
                }
            }
        }
        return stable;
    }
}
