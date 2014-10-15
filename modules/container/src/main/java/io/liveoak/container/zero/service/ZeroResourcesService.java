package io.liveoak.container.zero.service;

import java.io.File;

import io.liveoak.common.DefaultMountPointResource;
import io.liveoak.container.extension.MediaTypeMountService;
import io.liveoak.container.extension.MountService;
import io.liveoak.container.tenancy.InternalApplication;
import io.liveoak.container.tenancy.InternalApplicationRegistry;
import io.liveoak.container.zero.SystemResource;
import io.liveoak.container.zero.extension.ZeroExtension;
import io.liveoak.spi.MediaType;
import io.liveoak.spi.Services;
import io.liveoak.spi.resource.MountPointResource;
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
import org.vertx.java.core.Vertx;

import static io.liveoak.spi.Services.APPLICATIONS_DIR;
import static io.liveoak.spi.Services.APPLICATION_REGISTRY;
import static io.liveoak.spi.Services.VERTX;

/**
 * @author Bob McWhirter
 * @author Ken Finnigan
 */
public class ZeroResourcesService implements Service<Void> {

    @Override
    public void start(StartContext context) throws StartException {
        ServiceTarget target = context.getChildTarget();

        ServiceName systemName = Services.resource(ZeroExtension.APPLICATION_ID, "system");
        target.addService(systemName, new ValueService<SystemResource>(new ImmediateValue<>(new SystemResource())))
                .install();

        MountService<RootResource> mount = new MountService<>();
        target.addService(systemName.append("mount"), mount)
                .addDependency(Services.applicationContext(ZeroExtension.APPLICATION_ID), MountPointResource.class, mount.mountPointInjector())
                .addDependency(systemName, RootResource.class, mount.resourceInjector())
                .install();

        ServiceName instanceName = Services.resource(ZeroExtension.APPLICATION_ID, "system-instances");
        target.addService(instanceName, new ValueService<DefaultMountPointResource>(new ImmediateValue<>(new DefaultMountPointResource("system-instances"))))
                .install();

        MountService<RootResource> instanceMount = new MountService<>();
        target.addService(instanceName.append("mount"), instanceMount)
                .addDependency(Services.applicationContext(ZeroExtension.APPLICATION_ID), MountPointResource.class, instanceMount.mountPointInjector())
                .addDependency(instanceName, RootResource.class, instanceMount.resourceInjector())
                .install();

        ServiceName applicationsName = Services.resource(ZeroExtension.APPLICATION_ID, "applications");
        ApplicationsResourceService applicationsResource = new ApplicationsResourceService();
        target.addService(applicationsName, applicationsResource)
                .addDependency(APPLICATION_REGISTRY, InternalApplicationRegistry.class, applicationsResource.applicationRegistryInjector())
                .install();

        MediaTypeMountService<RootResource> mediaTypeMount = new MediaTypeMountService<>(null, MediaType.JSON, true);
        target.addService(Services.defaultMount(applicationsName), mediaTypeMount)
                .addDependency(Services.applicationContext(ZeroExtension.APPLICATION_ID), MountPointResource.class, mediaTypeMount.mountPointInjector())
                .addDependency(applicationsName, RootResource.class, mediaTypeMount.resourceInjector())
                .install();

        // Install Local Applications Resource
        ServiceName localApplicationsName = Services.resource(ZeroExtension.APPLICATION_ID, "local-applications");
        LocalApplicationsResourceService localAppsResourceService = new LocalApplicationsResourceService();
        target.addService(localApplicationsName, localAppsResourceService)
                .addDependency(APPLICATION_REGISTRY, InternalApplicationRegistry.class, localAppsResourceService.applicationRegistryInjector())
                .addDependency(APPLICATIONS_DIR, File.class, localAppsResourceService.applicationDirectoryInjector())
                .addDependency(VERTX, Vertx.class, localAppsResourceService.vertxInjector())
                .install();

        MediaTypeMountService<RootResource> localAppMediaTypeMount = new MediaTypeMountService<>(null, MediaType.LOCAL_APP_JSON, false);
        target.addService(Services.defaultMount(localApplicationsName), localAppMediaTypeMount)
                .addDependency(Services.applicationContext(ZeroExtension.APPLICATION_ID), MountPointResource.class, localAppMediaTypeMount.mountPointInjector())
                .addDependency(localApplicationsName, RootResource.class, localAppMediaTypeMount.resourceInjector())
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
