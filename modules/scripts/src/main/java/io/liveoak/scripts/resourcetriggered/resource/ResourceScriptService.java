package io.liveoak.scripts.resourcetriggered.resource;

import io.liveoak.scripts.resourcetriggered.manager.ResourceScriptManager;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.jboss.msc.value.InjectedValue;
import org.vertx.java.core.Vertx;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 */
public class ResourceScriptService implements Service<ResourceScripts> {

    ResourceScripts resourceScripts;

    @Override
    public void start(StartContext startContext) throws StartException {
        this.resourceScripts = new ResourceScripts(scriptManagerInjector.getValue(), vertxInjector.getValue());
    }

    @Override
    public void stop(StopContext stopContext) {
        this.resourceScripts = null;
    }

    @Override
    public ResourceScripts getValue() throws IllegalStateException, IllegalArgumentException {
        return resourceScripts;
    }

    public InjectedValue<ResourceScriptManager> scriptManagerInjector = new InjectedValue<>();

    public InjectedValue<Vertx> vertxInjector = new InjectedValue<>();
}
