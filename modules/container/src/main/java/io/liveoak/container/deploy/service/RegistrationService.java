package io.liveoak.container.deploy.service;

import io.liveoak.spi.Container;
import io.liveoak.spi.InitializationException;
import io.liveoak.spi.resource.RootResource;
import org.jboss.msc.inject.Injector;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.jboss.msc.value.InjectedValue;

/**
 * @author Bob McWhirter
 */
public class RegistrationService implements Service<RootResource> {

    public RegistrationService() {
    }

    @Override
    public void start(StartContext context) throws StartException {
        this.containerInjector.getValue().registerResource(this.resourceInjector.getValue());
    }

    @Override
    public void stop(StopContext context) {
        this.containerInjector.getValue().unregisterResource(this.resourceInjector.getValue());
    }

    @Override
    public RootResource getValue() throws IllegalStateException, IllegalArgumentException {
        return this.resourceInjector.getValue();
    }

    public Injector<Container> containerInjector() {
        return this.containerInjector;
    }

    public Injector<RootResource> resourceInjector() {
        return this.resourceInjector;
    }

    private InjectedValue<Container> containerInjector = new InjectedValue<>();
    private InjectedValue<RootResource> resourceInjector = new InjectedValue<>();
}
