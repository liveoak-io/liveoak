package io.liveoak.scripts.resourcetriggered.interceptor;

import org.jboss.msc.service.Service;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 */
public class ScriptInterceptorService implements Service<ScriptInterceptor> {

    private ScriptInterceptor serverScriptInterceptor;

    @Override
    public void start(StartContext startContext) throws StartException {
        this.serverScriptInterceptor = new ScriptInterceptor();
    }

    @Override
    public void stop(StopContext stopContext) {
        this.serverScriptInterceptor = null;
    }

    @Override
    public ScriptInterceptor getValue() throws IllegalStateException, IllegalArgumentException {
        return this.serverScriptInterceptor;
    }

}
