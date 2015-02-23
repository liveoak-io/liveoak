package io.liveoak.container.extension.application;

import java.util.Properties;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.liveoak.container.extension.application.service.AdminResourceWrappingResourceService;
import io.liveoak.container.extension.service.InitializeResourceService;
import io.liveoak.container.service.MediaTypeMountService;
import io.liveoak.container.extension.service.RootResourceLifecycleService;
import io.liveoak.container.extension.application.service.SaveResourceConfigService;
import io.liveoak.container.tenancy.ApplicationConfigurationManager;
import io.liveoak.spi.client.Client;
import io.liveoak.spi.resource.MountPointResource;
import io.liveoak.spi.Application;
import io.liveoak.spi.MediaType;
import io.liveoak.spi.Services;
import io.liveoak.spi.extension.ApplicationExtensionContext;
import io.liveoak.spi.resource.RootResource;
import io.liveoak.spi.resource.async.Resource;
import org.jboss.msc.service.ServiceController;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.ServiceTarget;
import org.jboss.msc.service.ValueService;
import org.jboss.msc.value.ImmediateValue;

/**
 * @author Bob McWhirter
 * @author Ken Finnigan
 */
public class ApplicationExtensionContextImpl implements ApplicationExtensionContext {

    public ApplicationExtensionContextImpl(ServiceTarget target, InternalApplicationExtension appExtension, ServiceName publicMount, ServiceName adminMount, ObjectNode configuration, boolean boottime) {
        this.target = target;
        this.appExtension = appExtension;
        this.publicMount = publicMount;
        this.adminMount = adminMount;
        this.configuration = configuration;
        this.boottime = boottime;
    }

    public String extensionId() {
        return this.appExtension.extensionId();
    }

    public String resourceId() {
        return this.appExtension.resourceId();
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
    public void mountPublic() {
        mountPublic(Services.resource(application().id(), resourceId()));
    }

    @Override
    public void mountPublic(ServiceName publicName) {
        mountPublic(publicName, null);
    }

    @Override
    public void mountPublic(ServiceName publicName, MediaType mediaType) {
        mountPublic(publicName, mediaType, false);
    }

    @Override
    public void mountPublic(ServiceName publicName, MediaType mediaType, boolean makeDefault) {
        MediaTypeMountService<RootResource> mount = new MediaTypeMountService(this.appExtension.resourceId(), mediaType, makeDefault);
        ServiceController<? extends Resource> controller = this.target.addService(Services.defaultMount(publicName), mount)
                .addDependency(this.publicMount, MountPointResource.class, mount.mountPointInjector())
                .addDependency(publicName, RootResource.class, mount.resourceInjector())
                .install();
        this.appExtension.publicResourceController(controller);
    }

    @Override
    public void mountPublic(RootResource publicResource) {
        mountPublic(publicResource, null);
    }

    @Override
    public void mountPublic(RootResource publicResource, MediaType mediaType) {
        mountPublic(publicResource, mediaType, false);
    }

    @Override
    public void mountPublic(RootResource publicResource, MediaType mediaType, boolean makeDefault) {
        ValueService<RootResource> service = new ValueService<RootResource>(new ImmediateValue<>(publicResource));
        this.target.addService(Services.resource(application().id(), resourceId()), service)
                .install();
        mountPublic(Services.resource(application().id(), resourceId()), mediaType, makeDefault);
    }

    @Override
    public void mountPrivate() {
        mountPrivate(Services.adminResource(application().id(), resourceId()));
    }

    @Override
    public void mountPrivate(ServiceName privateName) {
        mountPrivate(privateName, null);
    }

    @Override
    public void mountPrivate(ServiceName privateName, MediaType mediaType) {
        mountPrivate(privateName, mediaType, false);
    }

    @Override
    public void mountPrivate(ServiceName privateName, MediaType mediaType, boolean makeDefault) {
        MediaTypeMountService<RootResource> mount = new MediaTypeMountService(this.resourceId(), mediaType, makeDefault);

        SaveResourceConfigService saveConfigService = new SaveResourceConfigService(this.resourceId(), this.extensionId(), this.boottime);
        target.addService(privateName.append("save-config"), saveConfigService)
                .addDependency(Services.applicationConfigurationManager(appExtension.application().id()), ApplicationConfigurationManager.class, saveConfigService.configurationManagerInjector())
                .addInjection(saveConfigService.configurationInjector(), this.configuration)
                .install();

        AdminResourceWrappingResourceService wrapper = new AdminResourceWrappingResourceService(this.appExtension);
        target.addService(privateName.append("wrapper"), wrapper)
                .addDependency(privateName.append("save-config"))
                .addDependency(privateName, RootResource.class, wrapper.resourceInjector())
                .addDependency(Services.applicationConfigurationManager(appExtension.application().id()), ApplicationConfigurationManager.class, wrapper.configurationManagerInjector())
                .addDependency(Services.applicationEnvironmentProperties(appExtension.application().id()), Properties.class, wrapper.environmentPropertiesInjector())
                .addDependency(Services.CLIENT, Client.class, wrapper.clientInjector())
                .install();

        InitializeResourceService configApply = new InitializeResourceService(this.appExtension);
        target.addService(privateName.append("apply-config"), configApply)
                .addDependency(privateName.append("wrapper"), RootResource.class, configApply.resourceInjector())
                .addInjection(configApply.applicationInjector(), this.application())
                .addInjection(configApply.configurationInjector(), this.configuration)
                .install();

        RootResourceLifecycleService lifecycle = new RootResourceLifecycleService();
        target.addService(privateName.append("lifecycle"), lifecycle)
                .addDependency(privateName.append("apply-config"))
                .addDependency(privateName, RootResource.class, lifecycle.resourceInjector())
                .install();

        ServiceController<? extends Resource> controller = this.target.addService(Services.defaultMount(privateName), mount)
                .addDependency(privateName.append("lifecycle"))
                .addDependency(this.adminMount, MountPointResource.class, mount.mountPointInjector())
                .addDependency(privateName.append("wrapper"), RootResource.class, mount.resourceInjector())
                .install();
        this.appExtension.adminResourceController(controller);
    }

    @Override
    public void mountPrivate(RootResource privateResource) {
        mountPrivate(privateResource, null);
    }

    @Override
    public void mountPrivate(RootResource privateResource, MediaType mediaType) {
        mountPrivate(privateResource, mediaType, false);
    }

    @Override
    public void mountPrivate(RootResource privateResource, MediaType mediaType, boolean makeDefault) {
        ValueService<RootResource> service = new ValueService<RootResource>(new ImmediateValue<>(privateResource));
        this.target.addService(Services.adminResource(application().id(), resourceId()), service)
                .install();
        mountPrivate(Services.adminResource(application().id(), resourceId()), mediaType, makeDefault);
    }

    private ServiceTarget target;
    private final ServiceName publicMount;
    private final ServiceName adminMount;
    private final InternalApplicationExtension appExtension;
    private final ObjectNode configuration;
    private final boolean boottime;

}
