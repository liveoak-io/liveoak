package io.liveoak.container.zero.service;

import java.io.File;

import io.liveoak.container.tenancy.InternalApplicationRegistry;
import io.liveoak.container.zero.GitApplicationsResource;
import org.jboss.msc.inject.Injector;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.jboss.msc.value.InjectedValue;

/**
 * @author Ken Finnigan
 */
public class GitApplicationsResourceService implements Service<GitApplicationsResource> {
    @Override
    public void start(StartContext context) throws StartException {
        this.resource = new GitApplicationsResource(this.applicationRegistryInjector.getValue(), this.applicationDirectoryInjector.getValue());
    }

    @Override
    public void stop(StopContext context) {
        this.resource = null;
    }

    @Override
    public GitApplicationsResource getValue() throws IllegalStateException, IllegalArgumentException {
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

    private GitApplicationsResource resource;
}
