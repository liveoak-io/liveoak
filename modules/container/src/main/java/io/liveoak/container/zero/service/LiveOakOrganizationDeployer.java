package io.liveoak.container.zero.service;

import io.liveoak.container.tenancy.InternalApplication;
import io.liveoak.container.tenancy.InternalOrganization;
import io.liveoak.container.tenancy.InternalOrganizationRegistry;
import io.liveoak.container.tenancy.OrganizationRegistry;
import org.jboss.msc.inject.Injector;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.jboss.msc.value.InjectedValue;

/**
 * @author Bob McWhirter
 */
public class LiveOakOrganizationDeployer implements Service<InternalOrganization> {

    @Override
    public void start(StartContext context) throws StartException {
        try {
            this.organization = this.organizationRegistryInjector.getValue().createOrganization("liveoak", "LiveOak");
            System.err.println( "** LiveOak organization deployed" );
        } catch (InterruptedException e) {
            throw new StartException(e);
        }
    }

    @Override
    public void stop(StopContext context) {
    }

    @Override
    public InternalOrganization getValue() throws IllegalStateException, IllegalArgumentException {
        return this.organization;
    }

    public Injector<InternalOrganizationRegistry> organizationRegistryInjector() {
        return this.organizationRegistryInjector;
    }

    private InjectedValue<InternalOrganizationRegistry> organizationRegistryInjector = new InjectedValue<>();
    private InternalOrganization organization;
}
