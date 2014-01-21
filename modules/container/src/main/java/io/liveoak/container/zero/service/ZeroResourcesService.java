package io.liveoak.container.zero.service;

import io.liveoak.container.extension.MountService;
import io.liveoak.container.tenancy.InternalApplication;
import io.liveoak.container.tenancy.InternalOrganizationRegistry;
import io.liveoak.container.tenancy.MountPointResource;
import io.liveoak.container.zero.SystemResource;
import io.liveoak.spi.LiveOak;
import io.liveoak.spi.resource.RootResource;
import org.jboss.msc.inject.Injector;
import org.jboss.msc.service.*;
import org.jboss.msc.value.ImmediateValue;
import org.jboss.msc.value.InjectedValue;

/**
 * @author Bob McWhirter
 */
public class ZeroResourcesService implements Service<Void> {

    @Override
    public void start(StartContext context) throws StartException {
        ServiceTarget target = context.getChildTarget();

        ServiceName orgsName = LiveOak.applicationResource("liveoak", "zero", "organizations");
        OrganizationsResourceService orgs = new OrganizationsResourceService();
        target.addService(orgsName, orgs)
                .addDependency(LiveOak.ORGANIZATION_REGISTRY, InternalOrganizationRegistry.class, orgs.organizationRegistryInjector())
                .install();

        mount(target, orgsName);

        ServiceName systemName = LiveOak.applicationResource("liveoak", "zero", "system");
        target.addService(systemName, new ValueService<SystemResource>(new ImmediateValue<>(new SystemResource())))
                .install();

        mount(target, systemName);
    }

    protected void mount(ServiceTarget target, ServiceName resourceName) {
        MountService<RootResource> mount = new MountService<>();

        target.addService(resourceName.append("mount"), mount)
                .addDependency(LiveOak.applicationContext("liveoak", "zero"), MountPointResource.class, mount.mountPointInjector())
                .addDependency(resourceName, RootResource.class, mount.resourceInjector())
                .install();
    }

    @Override
    public void stop(StopContext context) {

    }

    @Override
    public Void getValue() throws IllegalStateException, IllegalArgumentException {
        return null;
    }

    public Injector<InternalApplication> applicationInjector() {
        return this.applicationInjector;
    }

    private InjectedValue<InternalApplication> applicationInjector = new InjectedValue<>();
}
