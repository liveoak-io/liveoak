package io.liveoak.container.zero.service;

import io.liveoak.container.tenancy.ApplicationConfigurationManager;
import io.liveoak.container.zero.ApplicationClientsResource;
import io.liveoak.spi.resource.RootResource;
import org.jboss.msc.inject.Injector;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.jboss.msc.value.InjectedValue;

/**
 * @author Ken Finnigan
 */
public class ApplicationClientsResourceService implements Service<RootResource> {
    @Override
    public void start(StartContext context) throws StartException {
        this.resource = new ApplicationClientsResource(this.configManagerInjector.getValue());
    }

    @Override
    public void stop(StopContext context) {
        this.resource = null;
    }

    @Override
    public RootResource getValue() throws IllegalStateException, IllegalArgumentException {
        return this.resource;
    }

    public Injector<ApplicationConfigurationManager> configManagerInjector() {
        return this.configManagerInjector;
    }

    private InjectedValue<ApplicationConfigurationManager> configManagerInjector = new InjectedValue<>();
    private RootResource resource;
}
