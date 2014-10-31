package io.liveoak.container;

import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.jboss.logging.Logger;
import org.jboss.msc.service.ServiceController;

/**
 * @author Ken Finnigan
 */
public class AbstractContainerTest {

    protected static LiveOakSystem system;

    protected static boolean awaitStability() throws InterruptedException {
        // Default all calls to a 7 second timeout if not specified
        return awaitStability(7, TimeUnit.SECONDS);
    }

    protected static boolean awaitStability(int timeout, TimeUnit unit) throws InterruptedException {
        return awaitStability(timeout, unit, null, null);
    }

    protected static boolean awaitStability(long timeout, TimeUnit unit, Set<? super ServiceController<?>> failed, Set<? super ServiceController<?>> problem) throws InterruptedException {
        boolean stable = system.awaitStability(timeout, unit, failed, problem);
        if (!stable) {
            log.warn("awaitStability() may require an increased timeout duration.");
        }

        if (failed != null && !failed.isEmpty()) {
            Iterator<? super ServiceController<?>> failedIterator = failed.iterator();
            while (failedIterator.hasNext()) {
                ServiceController controller = (ServiceController) failedIterator.next();
                log.errorf(CONTROLLER_MESSAGE, controller.getName(), controller.getState(), controller.getSubstate(), controller.getMode());
                if (controller.getStartException() != null) {
                    controller.getStartException().printStackTrace();
                }
            }
        }

        if (problem != null && !problem.isEmpty()) {
            Iterator<? super ServiceController<?>> problemIterator = problem.iterator();
            while (problemIterator.hasNext()) {
                ServiceController controller = (ServiceController) problemIterator.next();
                log.errorf(CONTROLLER_MESSAGE, controller.getName(), controller.getState(), controller.getSubstate(), controller.getMode());
                if (controller.getStartException() != null) {
                    controller.getStartException().printStackTrace();
                }
            }
        }

        return stable;
    }

    private static String CONTROLLER_MESSAGE = "Controller %s is in State: %s, Substate: %s and Mode: %s";

    private static final Logger log = Logger.getLogger(AbstractContainerTest.class);
}
