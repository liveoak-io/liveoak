package io.liveoak.container.service.bootstrap;

import io.liveoak.container.service.PlatformManagerService;
import io.liveoak.container.service.VertxService;
import io.liveoak.container.tenancy.GlobalContext;
import io.liveoak.container.tenancy.InternalApplicationRegistry;
import io.liveoak.container.tenancy.service.ApplicationRegistryService;
import io.liveoak.container.tenancy.service.ApplicationsDeployerService;
import io.liveoak.container.tenancy.service.ApplicationsDirectoryService;
import org.jboss.msc.inject.Injector;
import org.jboss.msc.service.*;
import org.jboss.msc.value.ImmediateValue;
import org.jboss.msc.value.InjectedValue;
import org.vertx.java.core.Vertx;
import org.vertx.java.platform.PlatformManager;

import java.io.File;

import static io.liveoak.spi.LiveOak.*;

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
