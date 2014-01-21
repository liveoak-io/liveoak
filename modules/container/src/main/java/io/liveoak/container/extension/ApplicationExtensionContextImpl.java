package io.liveoak.container.extension;

import io.liveoak.container.tenancy.InternalApplicationExtension;
import io.liveoak.container.tenancy.MountPointResource;
import io.liveoak.spi.Application;
import io.liveoak.spi.extension.ApplicationExtensionContext;
import io.liveoak.spi.resource.RootResource;
import io.liveoak.spi.resource.async.Resource;
import org.jboss.msc.service.ServiceController;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.ServiceTarget;

/**
 * @author Bob McWhirter
 */
public class ApplicationExtensionContextImpl implements ApplicationExtensionContext {

    public ApplicationExtensionContextImpl(ServiceTarget target, InternalApplicationExtension appExtension, String extensionId, ServiceName configurationServiceName, ServiceName publicMount, ServiceName adminMount) {
        this.target = target;
        this.appExtension = appExtension;
        this.extensionId = extensionId;
        this.configurationServiceName = configurationServiceName;
        this.publicMount = publicMount;
        this.adminMount = adminMount;
    }

    public String id() {
        return this.extensionId;
    }

    @Override
    public Application application() {
        return this.appExtension.application();
    }

    @Override
    public ServiceTarget target() {
        return this.target;
    }

    @Override
    public ServiceName configurationServiceName() {
        return this.configurationServiceName;
    }

    @Override
    public void mountPublic(ServiceName publicName) {
        MountService<RootResource> mount = new MountService(this.extensionId);
        ServiceController<? extends Resource> controller = this.target.addService(publicName.append("mount"), mount)
                .addDependency(this.publicMount, MountPointResource.class, mount.mountPointInjector())
                .addDependency(publicName, RootResource.class, mount.resourceInjector())
                .install();
        this.appExtension.publicResourceController(controller);
    }

    @Override
    public void mountPrivate(ServiceName privateName) {
        MountService<RootResource> mount = new MountService();
        this.target.addService(privateName.append("mount"), mount)
                .addDependency(this.adminMount, MountPointResource.class, mount.mountPointInjector())
                .addDependency(privateName, RootResource.class, mount.resourceInjector())
                .install();
    }

    private ServiceTarget target;
    private final ServiceName configurationServiceName;
    private final ServiceName publicMount;
    private final ServiceName adminMount;
    private final InternalApplicationExtension appExtension;
    private final String extensionId;
}
