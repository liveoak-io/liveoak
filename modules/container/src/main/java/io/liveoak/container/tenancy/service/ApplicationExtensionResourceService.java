package io.liveoak.container.tenancy.service;

import io.liveoak.container.extension.MountService;
import io.liveoak.container.tenancy.InternalApplicationExtension;
import io.liveoak.container.tenancy.MountPointResource;
import io.liveoak.container.zero.ApplicationExtensionResource;
import io.liveoak.container.zero.ApplicationResource;
import io.liveoak.spi.LiveOak;
import io.liveoak.spi.resource.RootResource;
import io.liveoak.spi.resource.async.Resource;
import org.jboss.msc.inject.Injector;
import org.jboss.msc.service.*;
import org.jboss.msc.value.ImmediateValue;
import org.jboss.msc.value.InjectedValue;

/**
 * @author Bob McWhirter
 */
public class ApplicationExtensionResourceService implements Service<ApplicationExtensionResource> {

    public ApplicationExtensionResourceService(InternalApplicationExtension ext, ServiceName configName) {
        this.ext = ext;
        this.configName = configName;
    }

    @Override
    public void start(StartContext context) throws StartException {

        ServiceTarget target = context.getChildTarget();

        String orgId = this.ext.application().organization().id();
        String appId = this.ext.application().id();

        ServiceName name = LiveOak.applicationExtensionAdminResource(orgId, appId, this.ext.id());

        this.resource = new ApplicationExtensionResource( this.ext, context.getController().getServiceContainer(), this.configName );

        MountService<ApplicationExtensionResource> mount = new MountService<>();

        ServiceController<ApplicationExtensionResource> controller = target.addService(name.append("mount"), mount)
                .addInjectionValue(mount.mountPointInjector(), new ImmediateValue<>(this.applicationResourceInjector.getValue().extensionsResource()))
                .addInjectionValue(mount.resourceInjector(), this)
                .install();

        this.ext.adminResourceController( controller );
    }

    @Override
    public void stop(StopContext context) {
        this.ext.adminResourceController( null );
    }

    @Override
    public ApplicationExtensionResource getValue() throws IllegalStateException, IllegalArgumentException {
        return this.resource;
    }

    public Injector<ApplicationResource> applicationResourceInjector() {
        return this.applicationResourceInjector;
    }


    private InternalApplicationExtension ext;
    private InjectedValue<ApplicationResource> applicationResourceInjector = new InjectedValue<>();

    private ApplicationExtensionResource resource;
    private ServiceName configName;
}
