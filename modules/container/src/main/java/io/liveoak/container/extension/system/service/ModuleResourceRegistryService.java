package io.liveoak.container.extension.system.service;

import io.liveoak.container.extension.system.ExtensionConfigurationManager;
import io.liveoak.container.extension.system.ModuleResourceRegistry;
import io.liveoak.spi.extension.Extension;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.ServiceTarget;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.jboss.msc.value.InjectedValue;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 */
public class ModuleResourceRegistryService implements Service<ModuleResourceRegistry> {

    private ModuleResourceRegistry moduleResourceRegistry;
    private String id;
    private Extension extension;
    private ServiceTarget target;

    public ModuleResourceRegistryService(String id, Extension extension, ServiceTarget target) {
        this.id = id;
        this.extension = extension;
        this.target = target;
    }

    @Override
    public void start(StartContext context) throws StartException {
        moduleResourceRegistry = new ModuleResourceRegistry(id, extension, target, configurationInjector.getValue());
    }

    @Override
    public void stop(StopContext context) {
        moduleResourceRegistry = null;
    }

    @Override
    public ModuleResourceRegistry getValue() throws IllegalStateException, IllegalArgumentException {
        return moduleResourceRegistry;
    }

    public InjectedValue<ExtensionConfigurationManager> configurationInjector = new InjectedValue<>();
}
