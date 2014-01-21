package io.liveoak.container.tenancy.service;

import io.liveoak.container.tenancy.InternalOrganizationRegistry;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;

/**
 * @author Bob McWhirter
 */
public class OrganizationRegistryService implements Service<InternalOrganizationRegistry> {

    @Override
    public void start(StartContext context) throws StartException {
        this.registry = new InternalOrganizationRegistry( context.getChildTarget() );
    }

    @Override
    public void stop(StopContext context) {

    }

    @Override
    public InternalOrganizationRegistry getValue() throws IllegalStateException, IllegalArgumentException {
        return this.registry;
    }

    private InternalOrganizationRegistry registry;
}
