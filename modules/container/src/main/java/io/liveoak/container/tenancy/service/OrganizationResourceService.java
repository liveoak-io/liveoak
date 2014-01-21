package io.liveoak.container.tenancy.service;

import io.liveoak.container.extension.MountService;
import io.liveoak.container.tenancy.InternalOrganization;
import io.liveoak.container.tenancy.MountPointResource;
import io.liveoak.container.zero.OrganizationResource;
import io.liveoak.container.zero.ZeroServices;
import io.liveoak.spi.LiveOak;
import org.jboss.msc.service.*;

/**
 * @author Bob McWhirter
 */
public class OrganizationResourceService implements Service<OrganizationResource> {

    public OrganizationResourceService(InternalOrganization org) {
        this.org = org;
    }

    @Override
    public void start(StartContext context) throws StartException {
        this.resource = new OrganizationResource(this.org);

        ServiceTarget target = context.getChildTarget();
        ServiceName name = context.getController().getName();

        MountService<OrganizationResource> mount = new MountService<>();

        this.org.resourceController(target.addService(name.append("mount"), mount)
                .addInjectionValue(mount.resourceInjector(), this)
                .addDependency(LiveOak.applicationResource("liveoak", "zero", "organizations"), MountPointResource.class, mount.mountPointInjector())
                .install());
    }

    @Override
    public void stop(StopContext context) {
        this.resource = null;
    }

    @Override
    public OrganizationResource getValue() throws IllegalStateException, IllegalArgumentException {
        return this.resource;
    }

    private final InternalOrganization org;
    private OrganizationResource resource;

}
