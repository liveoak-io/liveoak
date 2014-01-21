package io.liveoak.container.extension;

import io.liveoak.container.tenancy.MountPointResource;
import org.jboss.msc.inject.Injector;
import org.jboss.msc.service.*;
import org.jboss.msc.value.InjectedValue;

/**
 * @author Bob McWhirter
 */
public class ExtensionConfigResourceService implements Service<ExtensionConfigResource> {

    public ExtensionConfigResourceService(String id, ServiceName mountPoint, ServiceName configName) {
        this.id = id;
        this.mountPoint = mountPoint;
        this.configName = configName;
    }

    @Override
    public void start(StartContext context) throws StartException {
        ServiceTarget target = context.getChildTarget();
        ServiceName name = context.getController().getName();

        this.resource = new ExtensionConfigResource(this.id, this.serviceContainerInjector.getValue(), this.configName);

        MountService<ExtensionConfigResource> mount = new MountService<>();

        target.addService(name.append("mount"), mount)
                .addInjectionValue(mount.resourceInjector(), this)
                .addDependency(this.mountPoint, MountPointResource.class, mount.mountPointInjector())
                .install();
    }

    @Override
    public void stop(StopContext context) {
        this.resource = null;
    }

    @Override
    public ExtensionConfigResource getValue() throws IllegalStateException, IllegalArgumentException {
        return this.resource;
    }

    public Injector<ServiceContainer> serviceContainerInjector() {
        return this.serviceContainerInjector;
    }

    private String id;
    private ExtensionConfigResource resource;
    private final ServiceName mountPoint;
    private final ServiceName configName;

    private InjectedValue<ServiceContainer> serviceContainerInjector = new InjectedValue<>();

}
