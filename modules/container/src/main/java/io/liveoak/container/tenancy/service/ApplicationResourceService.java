package io.liveoak.container.tenancy.service;

import io.liveoak.container.tenancy.ApplicationConfigurationManager;
import io.liveoak.container.tenancy.ApplicationResource;
import io.liveoak.container.tenancy.InternalApplication;
import io.liveoak.container.zero.ApplicationExtensionsResource;
import org.jboss.msc.inject.Injector;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.ServiceTarget;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.jboss.msc.service.ValueService;
import org.jboss.msc.value.ImmediateValue;
import org.jboss.msc.value.InjectedValue;

/**
 * @author Bob McWhirter
 */
public class ApplicationResourceService implements Service<ApplicationResource> {

    public ApplicationResourceService(InternalApplication app) {
        this.app = app;
    }

    @Override
    public void start(StartContext context) throws StartException {
        this.resource = new ApplicationResource(this.app, this.configManager.getValue());

        ServiceTarget target = context.getChildTarget();
        ServiceName name = context.getController().getName();

        target.addService(name.append("extensions"), new ValueService<ApplicationExtensionsResource>(new ImmediateValue<>(this.resource.extensionsResource())))
                .install();
    }

    @Override
    public void stop(StopContext context) {
        this.resource = null;
    }

    @Override
    public ApplicationResource getValue() throws IllegalStateException, IllegalArgumentException {
        return this.resource;
    }

    public Injector<ApplicationConfigurationManager> configInjector() {
        return this.configManager;
    }

    private final InternalApplication app;
    private ApplicationResource resource;
    private InjectedValue<ApplicationConfigurationManager> configManager = new InjectedValue<>();
}
