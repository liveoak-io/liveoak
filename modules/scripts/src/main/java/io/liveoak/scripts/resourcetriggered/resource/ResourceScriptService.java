package io.liveoak.scripts.resourcetriggered.resource;

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

    private ResourceScripts resourceScripts;

    public ResourceScriptService(String applicationId) {
        this.applicationId = applicationId;
    }

    @Override
    public void start(StartContext startContext) throws StartException {
        this.resourceScripts = new ResourceScripts(applicationId, scriptRegistryInjector.getValue(), vertxInjector.getValue());
    }

    @Override
    public void stop(StopContext stopContext) {
        this.resourceScripts = null;
    }

    @Override
    public ResourceScripts getValue() throws IllegalStateException, IllegalArgumentException {
        return resourceScripts;
    }

    public InjectedValue<ScriptRegistry> getScriptRegistryInjector() {
        return scriptRegistryInjector;
    }

    public InjectedValue<Vertx> getVertxInjector() {
        return vertxInjector;
    }

    private InjectedValue<ScriptRegistry> scriptRegistryInjector = new InjectedValue<>();
    private InjectedValue<Vertx> vertxInjector = new InjectedValue<>();

    private String applicationId;
}
