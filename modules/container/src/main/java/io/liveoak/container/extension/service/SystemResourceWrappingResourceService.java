package io.liveoak.container.extension.service;

import java.util.Properties;

import io.liveoak.container.extension.ExtensionConfigurationManager;
import io.liveoak.container.extension.SystemResourceWrappingResource;
import io.liveoak.spi.client.Client;
import io.liveoak.spi.resource.RootResource;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.jboss.msc.value.InjectedValue;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 */
public class SystemResourceWrappingResourceService implements Service<RootResource> {

    public SystemResourceWrappingResourceService() {
    }

    @Override
    public void start(StartContext context) throws StartException {
        this.persistingResource = new SystemResourceWrappingResource(this.managerInjector.getValue(), resourceInjector.getValue(), environmentPropertiesInjector.getValue(), clientInjector.getValue());
    }

    @Override
    public void stop(StopContext context) {
        this.persistingResource = null;
    }

    @Override
    public RootResource getValue() throws IllegalStateException, IllegalArgumentException {
        return this.persistingResource;
    }

    public InjectedValue<RootResource> resourceInjector = new InjectedValue<>();
    public InjectedValue<ExtensionConfigurationManager> managerInjector = new InjectedValue<>();
    public InjectedValue<Properties> environmentPropertiesInjector = new InjectedValue<>();
    public InjectedValue<Client> clientInjector = new InjectedValue<>();
    public RootResource persistingResource;

}
