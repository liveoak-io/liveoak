package io.liveoak.container.service;

import io.liveoak.container.deploy.DefaultDeployer;
import io.liveoak.spi.container.RootResourceFactory;
import org.jboss.msc.inject.Injector;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.jboss.msc.value.InjectedValue;

/**
 * @author Bob McWhirter
 */
public class RootResourceFactoryRegistrationService implements Service<Void> {
    @Override
    public void start(StartContext context) throws StartException {
        this.deployerInjector.getValue().registerRootResourceFactory( this.factoryInjector.getValue() );
    }

    @Override
    public void stop(StopContext context) {
        this.deployerInjector.getValue().unregisterRootResourceFactory( this.factoryInjector.getValue() );
    }

    @Override
    public Void getValue() throws IllegalStateException, IllegalArgumentException {
        return null;
    }

    public Injector<DefaultDeployer> deployerInjector() {
        return this.deployerInjector;
    }

    public InjectedValue<RootResourceFactory> factoryInjector() {
        return this.factoryInjector;
    }

    private InjectedValue<DefaultDeployer> deployerInjector = new InjectedValue<>();
    private InjectedValue<RootResourceFactory> factoryInjector = new InjectedValue<>();
}
