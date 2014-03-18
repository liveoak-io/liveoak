package io.liveoak.container.extension;

import io.liveoak.container.tenancy.ApplicationConfigurationManager;
import io.liveoak.spi.resource.RootResource;
import org.jboss.msc.inject.Injector;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.jboss.msc.value.InjectedValue;

/**
 * @author Bob McWhirter
 */
public class ConfigPersistingService implements Service<RootResource> {

    public ConfigPersistingService(String type) {
        this.type = type;
    }

    @Override
    public void start(StartContext context) throws StartException {
        this.persistingResource = new ConfigPersistingRootResource( this.type, this.managerInjector.getValue(), resourceInjector.getValue() );
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

    private String type;
    private InjectedValue<RootResource> resourceInjector = new InjectedValue<>();
    private InjectedValue<ApplicationConfigurationManager> managerInjector = new InjectedValue<>();
    private ConfigPersistingRootResource persistingResource;
}
