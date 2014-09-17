package io.liveoak.container.extension;

import java.util.Properties;

import io.liveoak.container.tenancy.ApplicationConfigurationManager;
import io.liveoak.container.tenancy.InternalApplicationExtension;
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
public class AdminResourceWrappingResourceService implements Service<RootResource> {

    public AdminResourceWrappingResourceService(InternalApplicationExtension extension, boolean boottime) {
        this.extension = extension;
        this.boottime = boottime;
    }

    @Override
    public void start(StartContext context) throws StartException {
        this.persistingResource = new AdminResourceWrappingResource(this.extension, this.managerInjector.getValue(), resourceInjector.getValue(), environmentPropertiesInjector.getValue(), this.boottime);
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

    private InternalApplicationExtension extension;
    private InjectedValue<RootResource> resourceInjector = new InjectedValue<>();
    private InjectedValue<ApplicationConfigurationManager> managerInjector = new InjectedValue<>();
    private InjectedValue<Properties> environmentPropertiesInjector = new InjectedValue<>();
    private AdminResourceWrappingResource persistingResource;
    private boolean boottime;
}
