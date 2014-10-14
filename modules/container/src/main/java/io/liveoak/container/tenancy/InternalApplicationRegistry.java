package io.liveoak.container.tenancy;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import io.liveoak.container.tenancy.service.ApplicationRemovalService;
import io.liveoak.container.tenancy.service.ApplicationService;
import io.liveoak.spi.Application;
import io.liveoak.spi.Services;
import org.jboss.msc.service.ServiceController;
import org.jboss.msc.service.ServiceTarget;
import org.vertx.java.core.Vertx;

/**
 * @author Bob McWhirter
 * @author Ken Finnigan
 */
public class InternalApplicationRegistry implements ApplicationRegistry {

    public InternalApplicationRegistry(ServiceTarget target) {
        this.target = target;
    }

    public InternalApplication createApplication(String id, String name) throws InterruptedException {
        return createApplication(id, name, null);
    }

    public InternalApplication createApplication(String id, String name, File directory) throws InterruptedException {
        return createApplication(id, name, directory, null);
    }

    public InternalApplication createApplication(String id, String name, File directory, Consumer<File> gitCommit) throws InterruptedException {
        ApplicationService app = new ApplicationService(id, name, directory, gitCommit);
        ServiceController<InternalApplication> controller = this.target.addService(Services.application(id), app)
                .addDependency(Services.APPLICATIONS_DIR, File.class, app.applicationsDirectoryInjector())
                .install();

        this.applications.put(id, controller);

        return controller.awaitValue();
    }

    @Override
    public Collection<InternalApplication> applications() {
        return Collections.unmodifiableCollection(this.applications.values().stream().map(e -> e.getValue()).collect(Collectors.toList()));
    }

    @Override
    public Application application(String id) throws InterruptedException {
        ServiceController<InternalApplication> controller = this.applications.get(id);
        if (controller == null) {
            return null;
        }
        return controller.awaitValue();
    }

    public void removeApplication(String id) {
        ServiceController<InternalApplication> controller = this.applications.remove(id);
        ApplicationRemovalService removalService = new ApplicationRemovalService(controller);
        this.target.addService(Services.application(id).append("remove"), removalService)
                .addDependency(Services.VERTX, Vertx.class, removalService.vertxInjector())
                .install();
    }

    private final ServiceTarget target;
    private Map<String, ServiceController<InternalApplication>> applications = new HashMap<>();
}
