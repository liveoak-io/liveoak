package io.liveoak.spi.extension;

import org.jboss.logging.Logger;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.ServiceController;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;

/**
 * @author Bob McWhirter
 */
public abstract class Task implements Service<Void> {

    private static final Logger log = Logger.getLogger(Task.class);

    protected abstract void perform() throws Exception;

    @Override
    public void start(StartContext context) throws StartException {
        try {
            perform();
            context.complete();
            context.getController().setMode(ServiceController.Mode.REMOVE);
        } catch (Exception e) {
            log.error("Unable to start scheduler task", e);
            throw new StartException(e);
        }
    }

    @Override
    public void stop(StopContext context) {
    }

    @Override
    public Void getValue() throws IllegalStateException, IllegalArgumentException {
        return null;
    }

}
