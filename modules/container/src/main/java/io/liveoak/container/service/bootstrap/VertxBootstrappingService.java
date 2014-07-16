package io.liveoak.container.service.bootstrap;

import io.liveoak.container.service.PlatformManagerService;
import io.liveoak.container.service.VertxService;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.ServiceTarget;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.vertx.java.platform.PlatformManager;

import static io.liveoak.spi.LiveOak.VERTX;
import static io.liveoak.spi.LiveOak.VERTX_PLATFORM_MANAGER;

/**
 * @author Bob McWhirter
 */
public class VertxBootstrappingService implements Service<Void> {
    @Override
    public void start(StartContext context) throws StartException {
        ServiceTarget target = context.getChildTarget();
        VertxService vertxSvc = new VertxService();
        target.addService(VERTX, vertxSvc)
                .addDependency(VERTX_PLATFORM_MANAGER, PlatformManager.class, vertxSvc.platformManagerInjector())
                .install();

        target.addService(VERTX_PLATFORM_MANAGER, new PlatformManagerService())
                .install();
    }

    @Override
    public void stop(StopContext context) {

    }

    @Override
    public Void getValue() throws IllegalStateException, IllegalArgumentException {
        return null;
    }

}
