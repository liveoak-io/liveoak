package io.liveoak.container.tenancy.service;

import io.liveoak.container.extension.MountService;
import io.liveoak.container.tenancy.InternalApplication;
import io.liveoak.container.zero.ApplicationResource;
import io.liveoak.container.zero.OrganizationResource;
import org.jboss.msc.inject.Injector;
import org.jboss.msc.service.*;
import org.jboss.msc.value.ImmediateValue;
import org.jboss.msc.value.InjectedValue;

/**
 * @author Bob McWhirter
 */
public class ApplicationResourceService implements Service<ApplicationResource> {

    public ApplicationResourceService(InternalApplication app) {
        this.app = app;
    }

    @Override
    public void start(StartContext context) throws StartException {

        this.resource = new ApplicationResource(this.app);

        ServiceTarget target = context.getChildTarget();
        ServiceName name = context.getController().getName();

        MountService<ApplicationResource> mount = new MountService<>();

        target.addService(name.append("mount"), mount)
                .addInjectionValue(mount.mountPointInjector(), new ImmediateValue<>(this.organizationResourceInjector.getValue().applicationsResource()))
                .addInjectionValue(mount.resourceInjector(), this)
                .install();
    }

    @Override
    public void stop(StopContext context) {
        this.resource = null;
    }

    @Override
    public ApplicationResource getValue() throws IllegalStateException, IllegalArgumentException {
        return this.resource;
    }

    public Injector<OrganizationResource> organizationResourceInjector() {
        return this.organizationResourceInjector;
    }

    private InjectedValue<OrganizationResource> organizationResourceInjector = new InjectedValue<>();
    private final InternalApplication app;
    private ApplicationResource resource;

}
