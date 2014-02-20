package io.liveoak.container.zero.service;

import io.liveoak.container.tenancy.InternalApplication;
import io.liveoak.container.tenancy.InternalApplicationRegistry;
import io.liveoak.container.zero.extension.ZeroExtension;
import org.jboss.msc.inject.Injector;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.jboss.msc.value.InjectedValue;

/**
 * @author Bob McWhirter
 */
public class ZeroApplicationDeployer implements Service<InternalApplication> {
    @Override
    public void start(StartContext context) throws StartException {
        try {
            System.err.println( "START APP: " + ZeroExtension.APPLICATION_ID );
            this.application = this.registryInjector.getValue().createApplication(ZeroExtension.APPLICATION_ID, ZeroExtension.APPLICATION_NAME );
        } catch (InterruptedException e) {
            throw new StartException(e);
        }
    }

    @Override
    public void stop(StopContext context) {
    }

    @Override
    public InternalApplication getValue() throws IllegalStateException, IllegalArgumentException {
        return this.application;
    }

    public Injector<InternalApplicationRegistry> applicationRegistryInjector() {
        return this.registryInjector;
    }

    private InjectedValue<InternalApplicationRegistry> registryInjector = new InjectedValue<>();
    private InternalApplication application;
}
