package io.liveoak.container.zero.service;

import io.liveoak.container.extension.MediaTypeMountService;
import io.liveoak.container.extension.MountService;
import io.liveoak.container.tenancy.InternalApplication;
import io.liveoak.container.tenancy.InternalApplicationRegistry;
import io.liveoak.container.tenancy.MountPointResource;
import io.liveoak.container.zero.SystemResource;
import io.liveoak.container.zero.extension.ZeroExtension;
import io.liveoak.spi.LiveOak;
import io.liveoak.spi.MediaType;
import io.liveoak.spi.resource.RootResource;
import org.jboss.msc.inject.Injector;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.ServiceTarget;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.jboss.msc.service.ValueService;
import org.jboss.msc.value.ImmediateValue;
import org.jboss.msc.value.InjectedValue;

/**
 * @author Bob McWhirter
 */
public class ZeroResourcesService implements Service<Void> {

    @Override
    public void start(StartContext context) throws StartException {
        ServiceTarget target = context.getChildTarget();

        ServiceName systemName = LiveOak.resource(ZeroExtension.APPLICATION_ID, "system");
        target.addService(systemName, new ValueService<SystemResource>(new ImmediateValue<>(new SystemResource())))
                .install();

        MountService<RootResource> mount = new MountService<>();
        target.addService(systemName.append("mount"), mount)
                .addDependency(LiveOak.applicationContext(ZeroExtension.APPLICATION_ID), MountPointResource.class, mount.mountPointInjector())
                .addDependency(systemName, RootResource.class, mount.resourceInjector())
                .install();

        ServiceName applicationsName = LiveOak.resource(ZeroExtension.APPLICATION_ID, "applications");
        ApplicationsResourceService applicationsResource = new ApplicationsResourceService();
        target.addService(applicationsName, applicationsResource)
                .addDependency(LiveOak.APPLICATION_REGISTRY, InternalApplicationRegistry.class, applicationsResource.applicationRegistryInjector())
                .install();

        MediaTypeMountService<RootResource> mediaTypeMount = new MediaTypeMountService<>(null, MediaType.JSON, true);
        target.addService(LiveOak.defaultMount(applicationsName), mediaTypeMount)
                .addDependency(LiveOak.applicationContext(ZeroExtension.APPLICATION_ID), MountPointResource.class, mediaTypeMount.mountPointInjector())
                .addDependency(applicationsName, RootResource.class, mediaTypeMount.resourceInjector())
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
