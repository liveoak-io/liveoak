package io.liveoak.container.tenancy;

import java.io.File;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.liveoak.common.util.ConversionUtils;
import io.liveoak.container.tenancy.service.ApplicationExtensionService;
import io.liveoak.spi.Application;
import io.liveoak.spi.LiveOak;
import io.liveoak.spi.ResourcePath;
import io.liveoak.spi.extension.Extension;
import io.liveoak.spi.state.ResourceState;
import org.jboss.logging.Logger;
import org.jboss.msc.service.ServiceController;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.ServiceRegistry;
import org.jboss.msc.service.ServiceTarget;
import org.jboss.msc.service.StabilityMonitor;
import org.jboss.msc.service.ValueService;
import org.jboss.msc.value.ImmediateValue;

/**
 * @author Bob McWhirter
 * @author Ken Finnigan
 */
public class InternalApplication implements Application {

    public InternalApplication(ServiceTarget target, String id, String name, File directory, ResourcePath htmlAppPath, Boolean visible) {
        this.target = target;
        this.id = id;
        this.name = name;
        this.directory = directory;
        this.htmlAppPath = htmlAppPath;
        this.visible = visible;
    }

    @Override
    public String id() {
        return this.id;
    }

    @Override
    public String name() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public File directory() {
        return this.directory;
    }

    @Override
    public Boolean visible() {
        return this.visible;
    }

    public void setVisible(Boolean visible) {
        this.visible = visible;
    }

    public File configurationFile() {
        return new File(this.directory, "application.json");
    }

    public ResourcePath htmlApplicationResourcePath() {
        return this.htmlAppPath;
    }

    public void setHtmlApplicationPath(String path) {
        this.htmlAppPath = new ResourcePath(path);
    }

    public InternalApplicationExtension extend(String resourceId, ResourceState resourceDefinition) throws Exception {
        return extend(resourceId, resourceDefinition, false);
    }

    public InternalApplicationExtension extend(String resourceId, ResourceState resourceDefinition, boolean boottime) throws Exception {
        String extensionId = (String) resourceDefinition.getProperty("type");

        ObjectNode config;

        ResourceState configState = (ResourceState) resourceDefinition.getProperty("config");
        if (configState != null) {
            config = ConversionUtils.convert(configState);
        } else {
            config = JsonNodeFactory.instance.objectNode();
        }

        return extend(extensionId, resourceId, config, boottime);
    }

    public InternalApplicationExtension extend(String extensionId) throws Exception {
        return extend(extensionId, JsonNodeFactory.instance.objectNode());
    }

    public InternalApplicationExtension extend(String extensionId, ObjectNode configuration) throws Exception {
        return extend(extensionId, extensionId, configuration);
    }

    public InternalApplicationExtension extend(String extensionId, String resourceId, ObjectNode configuration) throws Exception {
        return extend(extensionId, resourceId, configuration, false);
    }

    public InternalApplicationExtension extend(String extensionId, String resourceId, ObjectNode configuration, boolean boottime) throws Exception {

        ServiceTarget target = this.target.subTarget();
        StabilityMonitor monitor = new StabilityMonitor();
        target.addMonitor(monitor);

        ServiceName resourceServiceName = LiveOak.applicationExtension(this.id, resourceId);

        target.addService(resourceServiceName.append("config"), new ValueService<>(new ImmediateValue<>(configuration))).install();

        ApplicationExtensionService appExt = new ApplicationExtensionService(extensionId, resourceId, boottime);

        ServiceController<InternalApplicationExtension> controller = target.addService(resourceServiceName, appExt)
                .addDependency(resourceServiceName.append("config"), ObjectNode.class, appExt.configurationInjector())
                .addDependency(LiveOak.extension(extensionId), Extension.class, appExt.extensionInjector())
                .addDependency(LiveOak.application(this.id), InternalApplication.class, appExt.applicationInjector())
                .addDependency(LiveOak.SERVICE_REGISTRY, ServiceRegistry.class, appExt.serviceRegistryInjector())
                .install();

        monitor.awaitStability();

        InternalApplicationExtension intAppExt = controller.awaitValue();

        if (intAppExt.exception() != null) {
            intAppExt.remove();
            log.error(intAppExt.exception());
            throw intAppExt.exception();
        }

        target.removeMonitor(monitor);
        return intAppExt;
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
    private ResourcePath htmlAppPath;
    private Boolean visible;

    private static final Logger log = Logger.getLogger(InternalApplication.class);
}
