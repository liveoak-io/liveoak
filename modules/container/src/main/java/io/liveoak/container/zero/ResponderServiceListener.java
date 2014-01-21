package io.liveoak.container.zero;

import io.liveoak.spi.resource.async.Resource;
import io.liveoak.spi.resource.async.Responder;
import org.jboss.msc.service.AbstractServiceListener;
import org.jboss.msc.service.ServiceController;
import org.jboss.msc.service.ServiceListener;

/**
 * @author Bob McWhirter
 */
public class ResponderServiceListener<T extends Resource> extends AbstractServiceListener<T> {

    public ResponderServiceListener(Responder responder) {
        this.responder = responder;
    }

    @Override
    public void transition(ServiceController<? extends T> controller, ServiceController.Transition transition) {
        switch (transition.getAfter()) {
            case NEW:
                break;
            case CANCELLED:
                break;
            case DOWN:
                break;
            case WAITING:
                break;
            case WONT_START:
                break;
            case PROBLEM:
                this.responder.internalError(controller.getStartException());
                break;
            case START_REQUESTED:
                break;
            case START_INITIATING:
                break;
            case STARTING:
                break;
            case START_FAILED:
                this.responder.internalError(controller.getStartException());
                break;
            case UP:
                this.responder.resourceCreated(controller.getValue());
                break;
            case STOP_REQUESTED:
                break;
            case STOPPING:
                break;
            case REMOVING:
                this.responder.resourceDeleted( controller.getValue() );
                break;
            case REMOVED:
                break;
        }
        if (transition.entersRestState()) {
            controller.removeListener(this);
        }
    }

    private final Responder responder;
}
