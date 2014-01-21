package io.liveoak.container.tenancy;

import io.liveoak.container.extension.CommonExtensions;
import io.liveoak.container.tenancy.service.ApplicationService;
import io.liveoak.container.zero.OrganizationResource;
import io.liveoak.spi.LiveOak;
import io.liveoak.spi.Organization;
import org.jboss.msc.service.*;
import org.jboss.msc.value.ImmediateValue;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Bob McWhirter
 */
public class InternalOrganization implements Organization {

    public InternalOrganization(ServiceTarget target, String id, String name) {
        this.target = target;
        this.id = id;
        this.name = name;
        this.target.addDependency(LiveOak.organization(id));
    }

    @Override
    public String id() {
        return this.id;
    }

    public void id(String id) {
        this.id = id;
    }

    @Override
    public String name() {
        return this.name;
    }

    public void name(String name) {
        this.name = name;
    }

    @Override
    public Collection<InternalApplication> applications() {
        return Collections.unmodifiableCollection(this.applications.values().stream().map(e -> e.getValue()).collect(Collectors.toList()));
    }

    @Override
    public InternalApplication application(String id) {
        ServiceController<InternalApplication> controller = this.applications.get(id);
        if (controller == null) {
            return null;
        }
        return controller.getValue();
    }

    public InternalApplication createApplication(String id, String name) throws InterruptedException {
        return createApplication(id, name, null);
    }

    public InternalApplication createApplication(String id, String name, File directory) throws InterruptedException {
        ApplicationService app = new ApplicationService(id, name, directory);
        ServiceController<InternalApplication> controller = this.target.addService(LiveOak.application(this.id, id), app)
                .addDependency(LiveOak.organization(this.id), InternalOrganization.class, app.organizationInjector())
                .addDependency(LiveOak.APPLICATIONS_DIR, File.class, app.applicationsDirectoryInjector())
                .install();

        this.applications.put(id, controller);

        return controller.awaitValue();
    }

    public void destroyApplication(InternalApplication app) {
        ServiceController<InternalApplication> controller = this.applications.remove(app.id());
        controller.setMode(ServiceController.Mode.REMOVE);
    }

    public void contextController(ServiceController<OrganizationContext> contextController) {
        this.contextController = contextController;
    }

    public OrganizationContext context() throws InterruptedException {
        return this.contextController.awaitValue();
    }

    public void resourceController(ServiceController<OrganizationResource> resourceController) {
        this.resourceController = resourceController;
    }

    public OrganizationResource resource() throws InterruptedException {
        return this.resourceController.awaitValue();
    }

    private ServiceTarget target;

    private String id;
    private String name;

    private Map<String, ServiceController<InternalApplication>> applications = new HashMap<>();

    private ServiceController<OrganizationContext> contextController;
    private ServiceController<OrganizationResource> resourceController;
}
