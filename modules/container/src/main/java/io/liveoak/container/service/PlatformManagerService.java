package io.liveoak.container.service;

import org.jboss.msc.service.Service;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.vertx.java.platform.PlatformLocator;
import org.vertx.java.platform.PlatformManager;

/**
 * @author Bob McWhirter
 */
public class PlatformManagerService implements Service<PlatformManager> {

    public PlatformManagerService() {

    }

    @Override
    public void start(StartContext context) throws StartException {
        this.platformManager = PlatformLocator.factory.createPlatformManager();
    }

    @Override
    public void stop(StopContext context) {
        this.platformManager.stop();
    }

    @Override
    public PlatformManager getValue() throws IllegalStateException, IllegalArgumentException {
        return this.platformManager;
    }

    private PlatformManager platformManager;
}
