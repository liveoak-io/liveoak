package io.liveoak.container.tenancy.service;

import io.liveoak.container.extension.MountService;
import io.liveoak.container.tenancy.ApplicationContext;
import io.liveoak.container.tenancy.GlobalContext;
import io.liveoak.container.tenancy.InternalApplication;
import io.liveoak.container.tenancy.MountPointResource;
import io.liveoak.spi.LiveOak;
import org.jboss.msc.service.*;

/**
 * @author Bob McWhirter
 */
public class ApplicationContextService implements Service<ApplicationContext> {

    public ApplicationContextService(InternalApplication app) {
        this.app = app;
    }

    @Override
    public void start(StartContext context) throws StartException {
        this.context = new ApplicationContext(this.app);

        ServiceTarget target = context.getChildTarget();
        ServiceName name = context.getController().getName();

        target.addService(name.append("bootstrap"), new ApplicationContextBootstrapService(this.app))
                .install();
    }

    @Override
    public void stop(StopContext context) {
        this.context = null;
    }

    @Override
    public ApplicationContext getValue() throws IllegalStateException, IllegalArgumentException {
        return this.context;
    }

    private InternalApplication app;
    private ApplicationContext context;
}
