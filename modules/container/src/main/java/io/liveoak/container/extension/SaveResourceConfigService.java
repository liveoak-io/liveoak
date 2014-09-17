package io.liveoak.container.extension;

import java.io.IOException;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.liveoak.container.tenancy.ApplicationConfigurationManager;
import org.jboss.msc.inject.Injector;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.jboss.msc.value.InjectedValue;

/**
 * @author Ken Finnigan
 */
public class SaveResourceConfigService implements Service<Void> {

    public SaveResourceConfigService(String resourceId, String resourceType, boolean boottime) {
        this.resourceId = resourceId;
        this.resourceType = resourceType;
        this.boottime = boottime;
    }

    @Override
    public void start(StartContext context) throws StartException {
        // Only save the Resource config to a file when it's not boot, as this means we're creating a new Resource that
        // currently has no configuration saved
        if (!boottime) {
            try {
                this.managerInjector.getValue().updateResource(this.resourceId, this.resourceType, this.configurationInjector.getValue());
            } catch (IOException e) {
                throw new StartException(e);
            }
        }
    }

    @Override
    public void stop(StopContext context) {

    }

    @Override
    public Void getValue() {
        return null;
    }

    public Injector<ObjectNode> configurationInjector() {
        return this.configurationInjector;
    }

    public Injector<ApplicationConfigurationManager> configurationManagerInjector() {
        return this.managerInjector;
    }

    private InjectedValue<ObjectNode> configurationInjector = new InjectedValue<>();
    private InjectedValue<ApplicationConfigurationManager> managerInjector = new InjectedValue<>();
    private String resourceId;
    private String resourceType;
    private boolean boottime;
}
