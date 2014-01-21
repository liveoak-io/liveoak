package io.liveoak.container.tenancy.service;

import io.liveoak.container.tenancy.InternalOrganization;
import io.liveoak.spi.LiveOak;
import org.jboss.msc.service.*;

/**
 * @author Bob McWhirter
 */
public class OrganizationService implements Service<InternalOrganization> {

    public OrganizationService(String id, String name) {
        this.id = id;
        this.name = name;
    }

    @Override
    public void start(StartContext context) throws StartException {
        ServiceTarget target = context.getChildTarget();

        this.org = new InternalOrganization(target, this.id, this.name);

        OrganizationContextService orgContext = new OrganizationContextService(this.org);
        this.org.contextController(target.addService(LiveOak.organizationContext(this.org.id()), orgContext)
                .install());

        OrganizationResourceService orgResource = new OrganizationResourceService(this.org);
        this.org.resourceController(target.addService(LiveOak.organizationAdminResource(this.org.id()), orgResource)
                .install());
    }

    @Override
    public void stop(StopContext context) {

    }

    @Override
    public InternalOrganization getValue() throws IllegalStateException, IllegalArgumentException {
        return this.org;
    }

    private String id;
    private String name;
    private InternalOrganization org;
}
