package io.liveoak.container.zero.service;

import io.liveoak.container.tenancy.InternalOrganizationRegistry;
import io.liveoak.container.tenancy.OrganizationRegistry;
import io.liveoak.container.zero.OrganizationsResource;
import org.jboss.msc.inject.Injector;
import org.jboss.msc.service.*;
import org.jboss.msc.value.InjectedValue;

/**
 * @author Bob McWhirter
 */
public class OrganizationsResourceService implements Service<OrganizationsResource> {

    @Override
    public void start(StartContext context) throws StartException {
        this.resource = new OrganizationsResource(
                this.organizationRegistryInjector.getValue()
        );
    }

    @Override
    public void stop(StopContext context) {
        this.resource = null;
    }

    @Override
    public OrganizationsResource getValue() throws IllegalStateException, IllegalArgumentException {
        return this.resource;
    }

    public Injector<InternalOrganizationRegistry> organizationRegistryInjector() {
        return this.organizationRegistryInjector;
    }

    private InjectedValue<InternalOrganizationRegistry> organizationRegistryInjector = new InjectedValue<>();

    private OrganizationsResource resource;
}
