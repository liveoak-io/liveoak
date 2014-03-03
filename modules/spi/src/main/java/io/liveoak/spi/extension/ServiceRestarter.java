package io.liveoak.spi.extension;

import org.jboss.msc.service.AbstractServiceListener;
import org.jboss.msc.service.ServiceController;

import java.util.function.Consumer;

/**
 * @author Bob McWhirter
 */
public class ServiceRestarter extends AbstractServiceListener {

    private enum Direction {
        DOWNWARDS,
        UPWARDS,
    }

    private final ServiceController<?> controller;
    private Consumer<ServiceController<?>> callback;
    private ServiceController.Mode mode;

    private Direction direction = Direction.DOWNWARDS;

    public static void restart(ServiceController<?> controller) {
        new ServiceRestarter(controller).restart();
    }

    public static void restart(ServiceController<?> controller, Consumer<ServiceController<?>> callback) {
        new ServiceRestarter(controller, callback).restart();
    }

    public ServiceRestarter(ServiceController<?> controller) {
        this.controller = controller;
    }

    public ServiceRestarter(ServiceController<?> controller, Consumer<ServiceController<?>> callback) {
        this.controller = controller;
        this.callback = callback;
    }

    public void restart() {
        this.controller.addListener(this);
        this.mode = this.controller.getMode();
        this.controller.setMode(ServiceController.Mode.NEVER);
    }

    @Override
    public void transition(ServiceController controller, ServiceController.Transition transition) {
        if (this.direction == Direction.DOWNWARDS) {
            if (transition.getAfter().equals(ServiceController.Substate.DOWN)) {
                this.direction = Direction.UPWARDS;
                this.controller.setMode(this.mode);
            }
        } else if (this.direction == Direction.UPWARDS) {
            if (transition.entersRestState()) {
                this.controller.removeListener(this);
                this.callback.accept(controller);
            }
        }
    }
}
