package io.liveoak.scripts.resourcetriggered.resource;

import org.jboss.msc.service.Service;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 */
public class ScriptMapService implements Service<ScriptMap> {

    ScriptMap scriptMap;

    @Override
    public void start(StartContext context) throws StartException {
        this.scriptMap = new ScriptMap();
    }

    @Override
    public void stop(StopContext context) {
        this.scriptMap = null;
    }

    @Override
    public ScriptMap getValue() throws IllegalStateException, IllegalArgumentException {
        return this.scriptMap;
    }
}
