package io.liveoak.container.tenancy.service;

import io.liveoak.container.tenancy.GlobalContext;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;

/**
 * @author Bob McWhirter
 */
public class GlobalContextService implements Service<GlobalContext> {

    @Override
    public void start(StartContext context) throws StartException {
        this.context = new GlobalContext();
    }

    @Override
    public void stop(StopContext context) {
        this.context = null;
    }

    @Override
    public GlobalContext getValue() throws IllegalStateException, IllegalArgumentException {
        return this.context;
    }

    private GlobalContext context;
}
