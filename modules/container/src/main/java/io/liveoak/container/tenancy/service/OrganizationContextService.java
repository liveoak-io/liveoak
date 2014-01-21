package io.liveoak.container.tenancy.service;

import io.liveoak.container.extension.MountService;
import io.liveoak.container.tenancy.InternalOrganization;
import io.liveoak.container.tenancy.MountPointResource;
import io.liveoak.spi.LiveOak;
import io.liveoak.container.tenancy.OrganizationContext;
import org.jboss.msc.service.*;

/**
 * @author Bob McWhirter
 */
public class OrganizationContextService implements Service<OrganizationContext> {

    public OrganizationContextService(InternalOrganization org) {
        this.org = org;
    }

    @Override
    public void start(StartContext context) throws StartException {
        this.context = new OrganizationContext(this.org.id());

        ServiceTarget target = context.getChildTarget();
        ServiceName name = context.getController().getName();

        MountService<OrganizationContext> mount = new MountService<>();

        target.addService(name.append("mount"), mount)
                .addInjectionValue(mount.resourceInjector(), this)
                .addDependency(LiveOak.GLOBAL_CONTEXT, MountPointResource.class, mount.mountPointInjector())
                .install();
    }

    @Override
    public void stop(StopContext context) {

    }

    @Override
    public OrganizationContext getValue() throws IllegalStateException, IllegalArgumentException {
        return this.context;
    }

    private InternalOrganization org;
    private OrganizationContext context;
}
