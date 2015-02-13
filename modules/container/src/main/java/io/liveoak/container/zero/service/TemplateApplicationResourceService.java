package io.liveoak.container.zero.service;

import io.liveoak.applications.templates.TemplateRegistry;
import io.liveoak.container.tenancy.InternalApplicationRegistry;
import io.liveoak.container.zero.TemplateApplicationResource;
import org.jboss.msc.inject.Injector;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.jboss.msc.value.InjectedValue;

import java.io.File;

/**
 * Created by mwringe on 11/02/15.
 */
public class TemplateApplicationResourceService implements Service<TemplateApplicationResource> {

    private TemplateApplicationResource resource;
    private InjectedValue<InternalApplicationRegistry> applicationRegistryInjector = new InjectedValue<>();
    private InjectedValue<File> applicationDirectoryInjector = new InjectedValue<>();
    private InjectedValue<TemplateRegistry> applicationTemplateInjector = new InjectedValue<>();

    @Override
    public void start(StartContext context) throws StartException {
        resource = new TemplateApplicationResource(this.applicationRegistryInjector.getValue(), applicationTemplateInjector.getValue(), this.applicationDirectoryInjector.getValue());
    }

    @Override
    public void stop(StopContext context) {
        this.resource = null;
    }

    @Override
    public TemplateApplicationResource getValue() throws IllegalStateException, IllegalArgumentException {
        return resource;
    }

    public Injector<InternalApplicationRegistry> applicationRegistryInjector() {
        return this.applicationRegistryInjector;
    }

    public Injector<File> applicationDirectoryInjector() {
        return this.applicationDirectoryInjector;
    }

    public Injector<TemplateRegistry> applicationTemplateRegistryInjector() {
        return this.applicationTemplateInjector;
    }
}
