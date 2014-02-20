package io.liveoak.container.tenancy;

import io.liveoak.container.tenancy.service.ApplicationService;
import io.liveoak.spi.Application;
import io.liveoak.spi.LiveOak;
import org.jboss.msc.service.ServiceController;
import org.jboss.msc.service.ServiceTarget;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Bob McWhirter
 */
public class InternalApplicationRegistry implements ApplicationRegistry {


    public InternalApplicationRegistry(ServiceTarget target) {
        this.target = target;
    }

    public InternalApplication createApplication(String id, String name) throws InterruptedException {
        return createApplication( id, name, null );
    }

    public InternalApplication createApplication(String id, String name, File directory) throws InterruptedException {
        ApplicationService app = new ApplicationService(id, name, directory);
        ServiceController<InternalApplication> controller = this.target.addService(LiveOak.application(id), app)
                .addDependency(LiveOak.APPLICATIONS_DIR, File.class, app.applicationsDirectoryInjector())
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
        if ( controller == null ) {
            return null;
        }
        return controller.awaitValue();
    }

    private final ServiceTarget target;
    private Map<String,ServiceController<InternalApplication>> applications = new HashMap<>();
}
