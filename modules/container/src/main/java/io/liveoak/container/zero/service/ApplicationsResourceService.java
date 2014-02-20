package io.liveoak.container.zero.service;

import io.liveoak.container.tenancy.ApplicationRegistry;
import io.liveoak.container.tenancy.InternalApplicationRegistry;
import io.liveoak.container.zero.ApplicationsResource;
import org.jboss.msc.inject.Injector;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.jboss.msc.value.InjectedValue;

/**
 * @author Bob McWhirter
 */
public class ApplicationsResourceService implements Service<ApplicationsResource> {
    @Override
    public void start(StartContext context) throws StartException {
        this.resource = new ApplicationsResource( this.applicationRegistryInjector.getValue() );
    }

    @Override
    public void stop(StopContext context) {
        this.resource = null;
    }

    @Override
    public ApplicationsResource getValue() throws IllegalStateException, IllegalArgumentException {
        return this.resource;
    }

    public Injector<InternalApplicationRegistry> applicationRegistryInjector() {
        return this.applicationRegistryInjector;
    }

    private InjectedValue<InternalApplicationRegistry> applicationRegistryInjector = new InjectedValue<>();
    private ApplicationsResource resource;
}
