package io.liveoak.applications.templates;

import org.jboss.msc.inject.Injector;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.jboss.msc.value.InjectedValue;

/**
 * Created by mwringe on 13/02/15.
 */
public class TemplateRegistryService implements Service<TemplateRegistry> {

    TemplateRegistry templateRegistry;

    private InjectedValue<ApplicationTemplateResource> templateResourceInjector = new InjectedValue<>();

    @Override
    public void start(StartContext context) throws StartException {
        templateRegistry = new TemplateRegistry();
        templateRegistry.addTemplateResource(templateResourceInjector.getValue());
    }

    @Override
    public void stop(StopContext context) {

    }

    @Override
    public TemplateRegistry getValue() throws IllegalStateException, IllegalArgumentException {
        return templateRegistry;
    }

    public Injector<ApplicationTemplateResource> applicationTemplateResourceInjector() {
        return this.templateResourceInjector;
    }
}
