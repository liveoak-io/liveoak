package io.liveoak.container.tenancy;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.liveoak.container.tenancy.service.ApplicationExtensionResourceService;
import io.liveoak.container.tenancy.service.ApplicationExtensionService;
import io.liveoak.container.zero.ApplicationResource;
import io.liveoak.spi.Application;
import io.liveoak.spi.LiveOak;
import io.liveoak.spi.extension.Extension;
import org.jboss.msc.service.*;

import java.io.File;

/**
 * @author Bob McWhirter
 */
public class InternalApplication implements Application {

    public InternalApplication(ServiceTarget target, InternalOrganization org, String id, String name, File directory) {
        this.target = target;
        this.org = org;
        this.id = id;
        this.name = name;
        this.directory = directory;
    }

    @Override
    public String id() {
        return this.id;
    }

    @Override
    public String name() {
        return this.name;
    }

    @Override
    public File directory() {
        return this.directory;
    }

    @Override
    public InternalOrganization organization() {
        return this.org;
    }

    public InternalApplicationExtension extend(String extensionId) throws InterruptedException {
        return extend(extensionId, JsonNodeFactory.instance.objectNode());
    }

    public InternalApplicationExtension extend(String extensionId, ObjectNode configuration) throws InterruptedException {
        ServiceName name = LiveOak.applicationExtension(this.organization().id(), this.id, extensionId);
        ApplicationExtensionService appExt = new ApplicationExtensionService(extensionId, configuration);
        ServiceController<InternalApplicationExtension> controller = this.target.addService(name, appExt)
                .addDependency(LiveOak.application(this.org.id(), this.id), InternalApplication.class, appExt.applicationInjector())
                .addDependency(LiveOak.extension(extensionId), Extension.class, appExt.extensionInjector())
                .addDependency(LiveOak.SERVICE_REGISTRY, ServiceRegistry.class, appExt.serviceRegistryInjector())
                .addDependency(LiveOak.SERVICE_CONTAINER, ServiceContainer.class, appExt.serviceContainerInjector())
                .install();

        return controller.awaitValue();
    }

    public InternalApplicationExtension extend(String extensionId, Extension extension, ObjectNode configuration) throws InterruptedException {
        ServiceName name = LiveOak.applicationExtension(this.organization().id(), this.id, extensionId);
        ApplicationExtensionService appExt = new ApplicationExtensionService(extensionId, configuration);
        ServiceController<InternalApplicationExtension> controller = this.target.addService(name, appExt)
                .addDependency(LiveOak.application(this.org.id(), this.id), InternalApplication.class, appExt.applicationInjector())
                .addInjection( appExt.extensionInjector(), extension )
                .addDependency(LiveOak.SERVICE_REGISTRY, ServiceRegistry.class, appExt.serviceRegistryInjector())
                .addDependency(LiveOak.SERVICE_CONTAINER, ServiceContainer.class, appExt.serviceContainerInjector())
                .install();

        return controller.awaitValue();
    }

    public void contextController(ServiceController<ApplicationContext> contextController) {
        this.contextController = contextController;
    }


    public void resourceController(ServiceController<ApplicationResource> resourceController) {
        this.resourceController = resourceController;
    }

    public ApplicationContext context() throws InterruptedException {
        return this.contextController.awaitValue();
    }

    public ApplicationResource resource() throws InterruptedException {
        return this.resourceController.awaitValue();
    }

    private ServiceController<ApplicationContext> contextController;
    private ServiceController<ApplicationResource> resourceController;

    private ServiceTarget target;
    private InternalOrganization org;
    private String id;
    private String name;
    private File directory;
    private ApplicationContext context;

}
