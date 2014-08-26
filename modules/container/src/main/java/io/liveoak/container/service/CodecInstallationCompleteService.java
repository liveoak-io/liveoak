package io.liveoak.container.service;

import org.jboss.logging.Logger;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;

/**
 * @author Ken Finnigan
 */
public class CodecInstallationCompleteService implements Service<Void> {
    @Override
    public void start(StartContext context) throws StartException {
        //Do Nothing
        log.trace("Codec installation complete.");
    }

    @Override
    public void stop(StopContext context) {
    }

    @Override
    public Void getValue() throws IllegalStateException, IllegalArgumentException {
        return null;
    }

    static final Logger log = Logger.getLogger(CodecInstallationCompleteService.class);
}
