package io.liveoak.container.extension;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.liveoak.container.tenancy.MountPointResource;
import io.liveoak.spi.extension.SystemExtensionContext;
import io.liveoak.spi.resource.RootResource;
import io.liveoak.spi.resource.async.Resource;
import org.jboss.msc.service.ServiceController;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.ServiceTarget;

/**
 * @author Bob McWhirter
 */
public class SystemExtensionContextImpl implements SystemExtensionContext {

    public SystemExtensionContextImpl(ServiceTarget target, String id, ServiceName systemExtensionMount, ObjectNode configuration) {
        this.target = target;
        this.id = id;
        this.systemExtensionMount = systemExtensionMount;
        this.configuration = configuration;
    }

    @Override
    public String id() {
        return this.id;
    }

    @Override
    public ServiceTarget target() {
        return this.target;
    }

    @Override
    public void mountPrivate(ServiceName privateName) {
        MountService<RootResource> mount = new MountService(this.id);

        ConfigFilteringService configFilter = new ConfigFilteringService();

        target.addService( privateName.append( "filter-config" ), configFilter )
                .addInjection( configFilter.configurationInjector(), this.configuration )
                .install();

        UpdateResourceService configApply = new UpdateResourceService();
        target.addService(privateName.append("apply-config"), configApply)
                .addDependency(privateName, Resource.class, configApply.resourceInjector())
                .addDependency( privateName.append( "filter-config"), ObjectNode.class, configApply.configurationInjector() )
                .install();

        RootResourceLifecycleService lifecycle = new RootResourceLifecycleService();
        target.addService(privateName.append( "lifecycle" ), lifecycle )
                .addDependency( privateName.append( "apply-config" ) )
                .addDependency( privateName, RootResource.class, lifecycle.resourceInjector() )
                .install();

        ServiceController<? extends Resource> controller = this.target.addService(privateName.append("mount"), mount)
                .addDependency(privateName.append("lifecycle"))
                .addDependency(this.systemExtensionMount, MountPointResource.class, mount.mountPointInjector())
                .addDependency(privateName, RootResource.class, mount.resourceInjector())
                .install();
    }

    private ServiceTarget target;
    private String id;
    private ServiceName systemExtensionMount;
    private ObjectNode configuration;


}
