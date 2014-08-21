package io.liveoak.container.zero.service;

import io.liveoak.common.codec.DefaultResourceState;
import io.liveoak.container.tenancy.InternalApplication;
import io.liveoak.spi.state.ResourceState;
import org.jboss.logging.Logger;
import org.jboss.msc.inject.Injector;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.jboss.msc.value.InjectedValue;

/**
 * @author Ken Finnigan
 */
public class ApplicationClientsInstallService implements Service<Void> {

    @Override
    public void start(StartContext context) throws StartException {
        ResourceState appClientState = new DefaultResourceState();
        appClientState.putProperty("type", "application-clients");

        context.asynchronous();
        try {
            new Thread(() -> {
                try {
                    log.debug("BOOTTIME INSTALL OF: application-clients");
                    this.applicationInjector.getValue().extend("application-clients", appClientState, false);
                    context.complete();
                } catch (Throwable e) {
                    context.failed(new StartException(e));
                }
            }, "ApplicationClientsInstallService starter - " + this.applicationInjector.getValue().name()).start();
        } catch (Throwable e) {
            context.failed(new StartException(e));
        }
    }

    @Override
    public void stop(StopContext context) {
    }

    @Override
    public Void getValue() {
        return null;
    }

    public Injector<InternalApplication> applicationInjector() {
        return this.applicationInjector;
    }

    private InjectedValue<InternalApplication> applicationInjector = new InjectedValue<>();

    private static final Logger log = Logger.getLogger(ApplicationClientsInstallService.class);
}
