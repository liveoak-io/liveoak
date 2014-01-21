package io.liveoak.container.tenancy;

import io.liveoak.container.tenancy.service.ApplicationExtensionRemovalService;
import io.liveoak.container.tenancy.service.ApplicationExtensionService;
import io.liveoak.spi.LiveOak;
import io.liveoak.spi.extension.Extension;
import io.liveoak.spi.resource.async.Resource;
import org.jboss.msc.service.ServiceController;
import org.jboss.msc.service.ServiceRegistry;
import org.jboss.msc.service.ServiceTarget;

/**
 * @author Bob McWhirter
 */
public class InternalApplicationExtension {

    public InternalApplicationExtension(ServiceRegistry registry, InternalApplication app, String extensionId) {
        this.registry = registry;
        this.app = app;
        this.id = extensionId;
    }

    public InternalApplication application() {
        return this.app;
    }

    public String id() {
        return this.id;
    }

    public void remove() {
        // unmount them first
        this.adminResourceController.setMode(ServiceController.Mode.REMOVE );
        this.publicResourceController.setMode(ServiceController.Mode.REMOVE );

        String orgId = this.app.organization().id();
        String appId = this.app.id();
        ServiceController<InternalApplicationExtension> extController = (ServiceController<InternalApplicationExtension>) this.registry.getService(LiveOak.applicationExtension(orgId, appId, this.id));

        ApplicationExtensionRemovalService removal = new ApplicationExtensionRemovalService( extController );

        extController.getServiceContainer().addService( extController.getName().append( "remove" ), removal )
                .addDependency( LiveOak.extension( this.id ), Extension.class, removal.extensionInjector() )
                .install();
    }

    public void adminResourceController(ServiceController<? extends Resource> controller) {
        this.adminResourceController = controller;
    }

    public void publicResourceController(ServiceController<? extends Resource> controller) {
        this.publicResourceController = controller;
    }

    public Resource adminResource() throws InterruptedException {
        return this.adminResourceController.awaitValue();
    }

    public Resource publicResource() throws InterruptedException {
        return this.publicResourceController.awaitValue();
    }

    private ServiceRegistry registry;
    private InternalApplication app;
    private String id;

    private ServiceController<? extends Resource> adminResourceController;
    private ServiceController<? extends Resource> publicResourceController;
}
