package io.liveoak.container.tenancy;

import java.io.File;
import java.util.Collection;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import io.liveoak.common.DefaultMountPointResource;
import io.liveoak.container.extension.ExtensionInstaller;
import io.liveoak.container.extension.ExtensionService;
import io.liveoak.container.extension.MountService;
import io.liveoak.container.service.ClientService;
import io.liveoak.container.tenancy.service.ApplicationsDeployerService;
import io.liveoak.container.tenancy.service.ApplicationsDirectoryService;
import io.liveoak.container.zero.ApplicationExtensionsResource;
import io.liveoak.container.zero.ApplicationsResource;
import io.liveoak.container.zero.extension.ApplicationClientsExtension;
import io.liveoak.container.zero.extension.ZeroExtension;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.Services;
import io.liveoak.spi.resource.MountPointResource;
import io.liveoak.spi.resource.RootResource;
import io.liveoak.spi.resource.async.Resource;
import org.jboss.msc.service.ServiceContainer;
import org.jboss.msc.service.ServiceController;
import org.jboss.msc.service.ServiceController.Mode;
import org.jboss.msc.service.ServiceController.State;
import org.jboss.msc.service.ValueService;
import org.jboss.msc.value.ImmediateValue;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static io.liveoak.spi.Services.APPLICATIONS_DEPLOYER;
import static io.liveoak.spi.Services.APPLICATIONS_DIR;
import static io.liveoak.spi.Services.APPLICATION_REGISTRY;
import static io.liveoak.spi.Services.GLOBAL_CONTEXT;
import static io.liveoak.spi.Services.application;
import static io.liveoak.spi.Services.applicationAdminResource;
import static io.liveoak.spi.Services.applicationContext;
import static io.liveoak.spi.Services.defaultMount;
import static io.liveoak.spi.Services.resource;
import static org.fest.assertions.Assertions.assertThat;

/**
 * @author Bob McWhirter
 */
public class TenancyTest {

    private ServiceContainer serviceContainer;

    @Before
    public void setUpServiceContainer() throws Exception {
        this.serviceContainer = ServiceContainer.Factory.create();

        File appDir = new File(getClass().getClassLoader().getResource("apps").getFile());
        this.serviceContainer.addService(Services.APPLICATIONS_DIR, new ApplicationsDirectoryService(appDir))
                .install();

        this.serviceContainer.addService(Services.SERVICE_REGISTRY, new ValueService<>(new ImmediateValue<>(this.serviceContainer))).install();

        ExtensionInstaller installer = new ExtensionInstaller(this.serviceContainer.subTarget());
        installer.load("application-clients", new ApplicationClientsExtension());

        this.serviceContainer.addService(Services.CLIENT, new ClientService()).install();

        this.serviceContainer.awaitStability();
    }

    @After
    public void tearDownServiceContainer() {
        this.serviceContainer.shutdown();
    }

    @Test
    public void testBootstrapPiecemeal() throws Exception {
        InternalApplicationRegistry registry = new InternalApplicationRegistry(this.serviceContainer);
        this.serviceContainer.addService(Services.APPLICATION_REGISTRY, new ValueService<>(new ImmediateValue<>(registry)))
                .install();

        InternalApplication installedApp = registry.createApplication(ZeroExtension.APPLICATION_ID, ZeroExtension.APPLICATION_NAME);

        DefaultMountPointResource adminMount = new DefaultMountPointResource("admin-mount");
        this.serviceContainer.addService(Services.resource(ZeroExtension.APPLICATION_ID, "applications"), new ValueService<MountPointResource>(new ImmediateValue<>(adminMount)))
                .install();

        this.serviceContainer.awaitStability();

        ServiceController<InternalApplication> app = (ServiceController<InternalApplication>) this.serviceContainer.getService(application(ZeroExtension.APPLICATION_ID));

        assertThat(app).isNotNull();
        assertThat(app.getMode()).isEqualTo(Mode.ACTIVE);
        assertThat(app.getState()).isEqualTo(State.UP);

        ServiceController<ApplicationResource> appAdmin = (ServiceController<ApplicationResource>) this.serviceContainer.getService(applicationAdminResource(ZeroExtension.APPLICATION_ID));
        assertThat(appAdmin).isNotNull();
        assertThat(appAdmin.getMode()).isEqualTo(Mode.ACTIVE);
        assertThat(appAdmin.getState()).isEqualTo(State.UP);

        ServiceController<?> appAdminMount = this.serviceContainer.getService(defaultMount(applicationAdminResource(ZeroExtension.APPLICATION_ID)));
        assertThat(appAdminMount).isNotNull();
        assertThat(appAdminMount.getMode()).isEqualTo(Mode.ACTIVE);
        assertThat(appAdminMount.getState()).isEqualTo(State.UP);  // we are mounted

        ServiceController<ApplicationContext> appContext = (ServiceController<ApplicationContext>) this.serviceContainer.getService(defaultMount(applicationContext(ZeroExtension.APPLICATION_ID)));
        assertThat(appContext).isNotNull();
        assertThat(appContext.getMode()).isEqualTo(Mode.ACTIVE);
        assertThat(appContext.getState()).isEqualTo(State.DOWN); // our public side isn't mounted

        /*
        Collection<Resource> registeredApps = orgAdmin.getValue().applicationsResource().members();
        assertThat(registeredApps).isNotNull();
        assertThat(registeredApps).hasSize(1);
        assertThat(((ApplicationResource) registeredApps.iterator().next()).application()).isSameAs(app.getValue());
        */

        // now let's wire up the Zero app's /applications container, and it should all fall together.

        MountService appsMountService = new MountService();
        this.serviceContainer.addService(Services.defaultMount(resource(ZeroExtension.APPLICATION_ID, "applications")), appsMountService)
                .addDependency(applicationContext(ZeroExtension.APPLICATION_ID), MountPointResource.class, appsMountService.mountPointInjector())
                .addDependency(resource(ZeroExtension.APPLICATION_ID, "applications"), RootResource.class, appsMountService.resourceInjector())
                .install();

        // now install global context, and public end should fall together.

        GlobalContext globalContext = new GlobalContext();
        this.serviceContainer.addService(GLOBAL_CONTEXT, new ValueService<GlobalContext>(new ImmediateValue<>(globalContext)))
                .install();

        this.serviceContainer.awaitStability();

        RequestContext ctx = new RequestContext.Builder().build();
        Collection<? extends Resource> appResources = appContext.getValue().members(ctx);
        assertThat(appResources).isNotNull();
        assertThat(appResources).hasSize(1);


        this.serviceContainer.awaitStability();
        appContext = (ServiceController<ApplicationContext>) this.serviceContainer.getService(defaultMount(applicationContext(ZeroExtension.APPLICATION_ID)));
        assertThat(appContext).isNotNull();
        assertThat(appContext.getMode()).isEqualTo(Mode.ACTIVE);
        assertThat(appContext.getState()).isEqualTo(State.UP);


        assertThat(globalContext.member(ctx, ZeroExtension.APPLICATION_ID)).isSameAs(appContext.getValue());
        assertThat(appContext.getValue().parent()).isSameAs(globalContext);

        assertThat(installedApp.context()).isSameAs(appContext.getValue());
        assertThat(installedApp.resource()).isSameAs(appAdmin.getValue());

    }

    @Test
    public void testBootstrapUsingExtension() throws Exception {
        GlobalContext globalContext = new GlobalContext();
        this.serviceContainer.addService(Services.GLOBAL_CONTEXT, new ValueService<>(new ImmediateValue<>(globalContext)))
                .install();

        InternalApplicationRegistry registry = new InternalApplicationRegistry(this.serviceContainer);
        this.serviceContainer.addService(Services.APPLICATION_REGISTRY, new ValueService<>(new ImmediateValue<>(registry)))
                .install();

        ApplicationsDeployerService deployerService = new ApplicationsDeployerService();
        this.serviceContainer.addService(APPLICATIONS_DEPLOYER, deployerService)
                .addDependency(APPLICATIONS_DIR, File.class, deployerService.applicationsDirectoryInjector())
                .addDependency(APPLICATION_REGISTRY, InternalApplicationRegistry.class, deployerService.applicationRegistryInjector())
                .install();

        ExtensionService ext = new ExtensionService(ZeroExtension.APPLICATION_ID, new ZeroExtension(), JsonNodeFactory.instance.objectNode(), null);
        this.serviceContainer.addService(Services.extension(ZeroExtension.EXTENSION_ID), ext)
                .install();

        this.serviceContainer.awaitStability();
        this.serviceContainer.dumpServices();

        ServiceController<InternalApplication> app = (ServiceController<InternalApplication>) this.serviceContainer.getService(application(ZeroExtension.APPLICATION_ID));

        assertThat(app).isNotNull();
        assertThat(app.getMode()).isEqualTo(Mode.ACTIVE);
        assertThat(app.getState()).isEqualTo(State.UP);

        ServiceController<ApplicationResource> appAdmin = (ServiceController<ApplicationResource>) this.serviceContainer.getService(applicationAdminResource(ZeroExtension.APPLICATION_ID));
        assertThat(appAdmin).isNotNull();
        assertThat(appAdmin.getMode()).isEqualTo(Mode.ACTIVE);
        assertThat(appAdmin.getState()).isEqualTo(State.UP);

        ServiceController<?> appAdminMount = this.serviceContainer.getService(defaultMount(applicationAdminResource(ZeroExtension.APPLICATION_ID)));
        assertThat(appAdminMount).isNotNull();
        assertThat(appAdminMount.getMode()).isEqualTo(Mode.ACTIVE);
        assertThat(appAdminMount.getState()).isEqualTo(State.UP); // we are mounted, but our parent isn't

        ServiceController<ApplicationContext> appContext = (ServiceController<ApplicationContext>) this.serviceContainer.getService(applicationContext(ZeroExtension.APPLICATION_ID));
        assertThat(appContext).isNotNull();
        assertThat(appContext.getMode()).isEqualTo(Mode.ACTIVE);
        assertThat(appContext.getState()).isEqualTo(State.UP);

        ServiceController<?> appContextMount = this.serviceContainer.getService(defaultMount(applicationContext(ZeroExtension.APPLICATION_ID)));
        assertThat(appContextMount).isNotNull();
        assertThat(appContextMount.getMode()).isEqualTo(Mode.ACTIVE);
        assertThat(appContextMount.getState()).isEqualTo(State.UP);

        RequestContext ctx = new RequestContext.Builder().build();
        ApplicationsResource appsAdminResource = (ApplicationsResource) app.getValue().context().member(ctx, "applications");

        Collection<Resource> registeredApps = appsAdminResource.members(ctx);
        assertThat(registeredApps).isNotNull();
        assertThat(registeredApps).hasSize(1);
        assertThat(((ApplicationResource) registeredApps.iterator().next()).application()).isSameAs(app.getValue());

        Collection<? extends Resource> appResources = appContext.getValue().members(ctx);
        assertThat(appResources).isNotNull();
        assertThat(appResources).hasSize(2); //system, applications

        ApplicationResource zeroApp = (ApplicationResource) appsAdminResource.member(ctx, ZeroExtension.APPLICATION_ID);
        assertThat(zeroApp).isNotNull();

        ApplicationExtensionsResource extsResource = zeroApp.extensionsResource();
        assertThat(extsResource).isNotNull();
    }


}
