package io.liveoak.container.extension;

import java.util.Properties;

import io.liveoak.container.tenancy.ApplicationConfigurationManager;
import io.liveoak.container.tenancy.InternalApplicationExtension;
import io.liveoak.spi.client.Client;
import io.liveoak.spi.resource.RootResource;
import org.jboss.msc.inject.Injector;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.jboss.msc.value.InjectedValue;

/**
 * @author Bob McWhirter
 * @author Ken Finnigan
 */
public class AdminResourceWrappingResourceService implements Service<RootResource> {

    public AdminResourceWrappingResourceService(InternalApplicationExtension extension) {
        this.extension = extension;
    }

    @Override
    public void start(StartContext context) throws StartException {
        this.persistingResource = new AdminResourceWrappingResource(this.extension, this.managerInjector.getValue(), resourceInjector.getValue(), environmentPropertiesInjector.getValue(), clientInjector.getValue());
    }

    @Override
    public void stop(StopContext context) {
        this.persistingResource = null;
    }

    @Override
    public RootResource getValue() throws IllegalStateException, IllegalArgumentException {
        return this.persistingResource;
    }

    public Injector<RootResource> resourceInjector() {
        return this.resourceInjector;
    }

    public Injector<ApplicationConfigurationManager> configurationManagerInjector() {
        return this.managerInjector;
    }

    public Injector<Properties> environmentPropertiesInjector() {
        return this.environmentPropertiesInjector;
    }

    public Injector<Client> clientInjector() {
        return this.clientInjector;
    }

    private InternalApplicationExtension extension;
    private InjectedValue<RootResource> resourceInjector = new InjectedValue<>();
    private InjectedValue<ApplicationConfigurationManager> managerInjector = new InjectedValue<>();
    private InjectedValue<Properties> environmentPropertiesInjector = new InjectedValue<>();
    private InjectedValue<Client> clientInjector = new InjectedValue<>();
    private AdminResourceWrappingResource persistingResource;
}
