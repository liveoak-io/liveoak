package io.liveoak.scripts;

import io.liveoak.scripts.libraries.resources.ScriptLibraries;
import io.liveoak.scripts.resource.ScriptsRootResource;
import io.liveoak.scripts.resourcetriggered.resource.ResourceScripts;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.jboss.msc.value.InjectedValue;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 */
public class ScriptService implements Service<ScriptsRootResource> {

    String id;
    private ScriptsRootResource rootResource;

    public ScriptService(String id) {
        this.id = id;
    }

    @Override
    public void start(StartContext startContext) throws StartException {
        this.rootResource = new ScriptsRootResource(id, librariesResourceInjector.getValue(), resourceScriptsInjector.getValue());
    }

    @Override
    public void stop(StopContext stopContext) {
        this.rootResource = null;
    }

    @Override
    public ScriptsRootResource getValue() throws IllegalStateException, IllegalArgumentException {
        return this.rootResource;
    }

    public InjectedValue<ResourceScripts> resourceScriptsInjector = new InjectedValue<>();

    public InjectedValue<ScriptLibraries> librariesResourceInjector = new InjectedValue<>();
}
