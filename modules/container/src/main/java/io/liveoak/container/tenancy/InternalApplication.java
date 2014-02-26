package io.liveoak.container.tenancy;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.liveoak.container.tenancy.service.ApplicationExtensionService;
import io.liveoak.container.zero.ApplicationResource;
import io.liveoak.container.zero.extension.ZeroExtension;
import io.liveoak.spi.Application;
import io.liveoak.spi.LiveOak;
import io.liveoak.spi.ResourcePath;
import io.liveoak.spi.extension.Extension;
import org.jboss.msc.service.*;

import java.io.File;

/**
 * @author Bob McWhirter
 */
public class InternalApplication implements Application {

    public InternalApplication(ServiceTarget target, String id, String name, File directory, ResourcePath htmlAppPath) {
        this.target = target;
        this.id = id;
        this.name = name;
        this.directory = directory;
        this.htmlAppPath = htmlAppPath;
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

    public ResourcePath htmlApplicationResourcePath() {
        return this.htmlAppPath;
    }

    public InternalApplicationExtension extend(String extensionId) throws InterruptedException {
        return extend(extensionId, JsonNodeFactory.instance.objectNode());
    }

    public InternalApplicationExtension extend(String extensionId, ObjectNode configuration) throws InterruptedException {
        return extend(extensionId, extensionId, configuration);
    }

    public InternalApplicationExtension extend(String extensionId, String resourceId, ObjectNode configuration) throws InterruptedException {

        ServiceTarget target = this.target.subTarget();
        StabilityMonitor monitor = new StabilityMonitor();
        target.addMonitor( monitor );

        ServiceName name = LiveOak.applicationExtension(this.id, resourceId);
        ApplicationExtensionService appExt = new ApplicationExtensionService(extensionId, resourceId, configuration);

        ServiceController<InternalApplicationExtension> controller = target.addService(name, appExt)
                .addDependency(LiveOak.extension(extensionId), Extension.class, appExt.extensionInjector())
                .addDependency(LiveOak.application(this.id ), InternalApplication.class, appExt.applicationInjector())
                .addDependency(LiveOak.SERVICE_REGISTRY, ServiceRegistry.class, appExt.serviceRegistryInjector())
                .addDependency(LiveOak.SERVICE_CONTAINER, ServiceContainer.class, appExt.serviceContainerInjector())
                .install();

        monitor.awaitStability();

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
    private String id;
    private String name;
    private File directory;
    private final ResourcePath htmlAppPath;

}
