package io.liveoak.scripts.resourcetriggered.resource;

import org.jboss.msc.service.Service;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 */
public class ScriptRegistryService implements Service<ScriptRegistry> {

    private ScriptRegistry scriptRegistry;

    @Override
    public void start(StartContext context) throws StartException {
        this.scriptRegistry = new ScriptRegistry();
    }

    @Override
    public void stop(StopContext context) {
        this.scriptRegistry = null;
    }

    @Override
    public ScriptRegistry getValue() throws IllegalStateException, IllegalArgumentException {
        return this.scriptRegistry;
    }
}
