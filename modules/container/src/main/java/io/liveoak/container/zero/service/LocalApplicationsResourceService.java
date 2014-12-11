package io.liveoak.container.zero.service;

import java.io.File;

import io.liveoak.container.tenancy.InternalApplicationRegistry;
import io.liveoak.container.zero.LocalApplicationsResource;
import org.jboss.msc.inject.Injector;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.jboss.msc.value.InjectedValue;

/**
 * @author Ken Finnigan
 */
public class LocalApplicationsResourceService implements Service<LocalApplicationsResource> {
    @Override
    public void start(StartContext context) throws StartException {
        this.resource = new LocalApplicationsResource(this.applicationRegistryInjector.getValue(), this.applicationDirectoryInjector.getValue());
    }

    @Override
    public void stop(StopContext context) {
        this.resource = null;
    }

    @Override
    public LocalApplicationsResource getValue() throws IllegalStateException, IllegalArgumentException {
        return this.resource;
    }

    public Injector<InternalApplicationRegistry> applicationRegistryInjector() {
        return this.applicationRegistryInjector;
    }

    public Injector<File> applicationDirectoryInjector() {
        return this.applicationDirectoryInjector;
    }

    private InjectedValue<InternalApplicationRegistry> applicationRegistryInjector = new InjectedValue<>();
    private InjectedValue<File> applicationDirectoryInjector = new InjectedValue<>();

    private LocalApplicationsResource resource;
}
