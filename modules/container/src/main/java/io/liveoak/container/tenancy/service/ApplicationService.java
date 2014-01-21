package io.liveoak.container.tenancy.service;

import io.liveoak.container.extension.CommonExtensions;
import io.liveoak.container.tenancy.InternalApplication;
import io.liveoak.container.tenancy.InternalOrganization;
import io.liveoak.container.zero.OrganizationResource;
import io.liveoak.spi.LiveOak;
import org.jboss.msc.inject.Injector;
import org.jboss.msc.service.*;
import org.jboss.msc.value.InjectedValue;

import java.io.File;

/**
 * @author Bob McWhirter
 */
public class ApplicationService implements Service<InternalApplication> {

    public ApplicationService(String id, String name, File directory) {
        this.id = id;
        this.name = name;
        this.directory = directory;
    }

    @Override
    public void start(StartContext context) throws StartException {
        ServiceTarget target = context.getChildTarget();

        String orgId = this.organizationInjector.getValue().id();

        File appDir = this.directory;

        if (appDir == null) {
            appDir = new File(new File(this.applicationsDirectoryInjector.getValue(), orgId), this.id);
            appDir.mkdirs();
        }

        InternalOrganization org = this.organizationInjector.getValue();
        this.app = new InternalApplication(target, org, this.id, this.name, appDir);

        ApplicationContextService appContext = new ApplicationContextService(this.app);
        this.app.contextController(target.addService(LiveOak.applicationContext(orgId, this.id), appContext)
                .install());

        ApplicationResourceService appResource = new ApplicationResourceService(this.app);
        this.app.resourceController(target.addService(LiveOak.applicationAdminResource(orgId, this.id), appResource)
                .addDependency(LiveOak.organizationAdminResource(orgId), OrganizationResource.class, appResource.organizationResourceInjector())
                .install());

        CommonApplicationExtensionsService common = new CommonApplicationExtensionsService();
        target.addService(LiveOak.application(orgId, this.id).append("common-extensions"), common)
                .addDependency(ServiceBuilder.DependencyType.OPTIONAL, LiveOak.COMMON_EXTENSIONS, CommonExtensions.class, common.commonExtensionsInjector())
                .addInjectionValue(common.applicationInjector(), this)
                .addDependencies(LiveOak.EXTENSION_LOADER)
                .install();
    }

    @Override
    public void stop(StopContext context) {
        this.app = null;
    }

    @Override
    public InternalApplication getValue() throws IllegalStateException, IllegalArgumentException {
        return this.app;
    }

    public Injector<InternalOrganization> organizationInjector() {
        return this.organizationInjector;
    }

    public Injector<File> applicationsDirectoryInjector() {
        return this.applicationsDirectoryInjector;
    }

    private String id;
    private String name;
    private File directory;
    private InjectedValue<InternalOrganization> organizationInjector = new InjectedValue<>();
    private InjectedValue<File> applicationsDirectoryInjector = new InjectedValue<>();
    private InternalApplication app;
}
