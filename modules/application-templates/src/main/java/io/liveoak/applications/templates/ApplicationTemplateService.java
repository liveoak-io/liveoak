package io.liveoak.applications.templates;


import org.jboss.msc.service.Service;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.jboss.msc.value.InjectedValue;

/**
 * Created by mwringe on 10/02/15.
 */
public class ApplicationTemplateService implements Service<ApplicationTemplateResource> {

    private ApplicationTemplateResource templateResource;

    public InjectedValue<TemplateRegistry> templateRegistryInjector = new InjectedValue<>();

    private String id;

    public ApplicationTemplateService(String id) {
        this.id = id;
    }

    @Override
    public void start(StartContext context) throws StartException {

        templateResource = new ApplicationTemplateResource(id);

        templateRegistryInjector.getValue().addTemplateResource(templateResource);
    }

    @Override
    public void stop(StopContext context) {
        // does nothing for now
    }

    @Override
    public ApplicationTemplateResource getValue() throws IllegalStateException, IllegalArgumentException {
        return templateResource;
    }
}
