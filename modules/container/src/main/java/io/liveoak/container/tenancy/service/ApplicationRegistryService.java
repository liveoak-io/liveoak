package io.liveoak.container.tenancy.service;

import io.liveoak.container.tenancy.InternalApplicationRegistry;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;

/**
 * @author Bob McWhirter
 */
public class ApplicationRegistryService implements Service<InternalApplicationRegistry> {


    @Override
    public void start(StartContext context) throws StartException {
        this.registry = new InternalApplicationRegistry( context.getChildTarget() );
    }

    @Override
    public void stop(StopContext context) {
        this.registry = null;
    }

    @Override
    public InternalApplicationRegistry getValue() throws IllegalStateException, IllegalArgumentException {
        return this.registry;
    }

    private InternalApplicationRegistry registry;
}
