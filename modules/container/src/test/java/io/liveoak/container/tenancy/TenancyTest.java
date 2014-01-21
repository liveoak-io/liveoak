package io.liveoak.container.tenancy;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import io.liveoak.container.extension.ExtensionService;
import io.liveoak.container.extension.MountService;
import io.liveoak.container.tenancy.service.ApplicationsDirectoryService;
import io.liveoak.container.zero.*;
import io.liveoak.container.zero.extension.ZeroExtension;
import io.liveoak.spi.LiveOak;
import io.liveoak.spi.extension.Task;
import io.liveoak.spi.resource.RootResource;
import io.liveoak.spi.resource.async.Resource;
import org.jboss.msc.service.*;
import org.jboss.msc.service.ServiceController.Mode;
import org.jboss.msc.service.ServiceController.State;
import org.jboss.msc.value.ImmediateValue;
import org.jboss.msc.value.InjectedValue;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Collection;

import static io.liveoak.spi.LiveOak.*;

import static org.fest.assertions.Assertions.assertThat;

/**
 * @author Bob McWhirter
 */
public class TenancyTest {

    private ServiceContainer serviceContainer;

    @Before
    public void setUpServiceContainer() {
        this.serviceContainer = ServiceContainer.Factory.create();

        SimpleResourceRegistry adminMount = new SimpleResourceRegistry("admin-mount" );
        this.serviceContainer.addService( ServiceName.of( "admin-mount" ), new ValueService<MountPointResource>( new ImmediateValue<>( adminMount )))
                .install();

        this.serviceContainer.addService( LiveOak.APPLICATIONS_DIR, new ApplicationsDirectoryService(null))
                .install();
    }

    @After
    public void tearDownServiceContainer() {
        this.serviceContainer.shutdown();
    }

    @Test
    public void testBootstrapPiecemeal() throws InterruptedException {
        InternalOrganizationRegistry registry = new InternalOrganizationRegistry(this.serviceContainer);

        InternalOrganization installedOrg = registry.createOrganization("liveoak", "LiveOak");

        this.serviceContainer.awaitStability();

        ServiceController<InternalOrganization> org = (ServiceController<InternalOrganization>) this.serviceContainer.getService(organization("liveoak"));

        assertThat(org).isNotNull();
        assertThat(org.getMode()).isEqualTo(Mode.ACTIVE);
        assertThat(org.getState()).isEqualTo(State.UP);

        ServiceController<OrganizationResource> orgAdmin = (ServiceController<OrganizationResource>) this.serviceContainer.getService(organizationAdminResource("liveoak"));

        assertThat(orgAdmin).isNotNull();
        assertThat(orgAdmin.getMode()).isEqualTo(Mode.ACTIVE);
        assertThat(orgAdmin.getState()).isEqualTo(State.UP);

        ServiceController<?> orgAdminMount = this.serviceContainer.getService(LiveOak.organizationAdminResource("liveoak").append("mount"));
        assertThat(orgAdminMount).isNotNull();
        assertThat(orgAdminMount.getMode()).isEqualTo(Mode.ACTIVE);
        assertThat(orgAdminMount.getState()).isEqualTo(State.DOWN);

        ServiceController<OrganizationContext> orgContext = (ServiceController<OrganizationContext>) this.serviceContainer.getService(organizationContext("liveoak"));
        assertThat(orgContext).isNotNull();
        assertThat(orgContext.getMode()).isEqualTo(Mode.ACTIVE);
        assertThat(orgContext.getState()).isEqualTo(State.UP);

        ServiceController<?> orgContextMount = this.serviceContainer.getService(LiveOak.organizationContext("liveoak").append("mount"));
        assertThat(orgContextMount).isNotNull();
        assertThat(orgContextMount.getMode()).isEqualTo(Mode.ACTIVE);
        assertThat(orgContextMount.getState()).isEqualTo(State.DOWN);

        //

        InternalApplication installedApp = installedOrg.createApplication("zero", "Zero");

        this.serviceContainer.awaitStability();

        ServiceController<InternalApplication> app = (ServiceController<InternalApplication>) this.serviceContainer.getService(application("liveoak", "zero"));

        assertThat(app).isNotNull();
        assertThat(app.getMode()).isEqualTo(Mode.ACTIVE);
        assertThat(app.getState()).isEqualTo(State.UP);

        ServiceController<ApplicationResource> appAdmin = (ServiceController<ApplicationResource>) this.serviceContainer.getService(applicationAdminResource("liveoak", "zero"));
        assertThat(appAdmin).isNotNull();
        assertThat(appAdmin.getMode()).isEqualTo(Mode.ACTIVE);
        assertThat(appAdmin.getState()).isEqualTo(State.UP);

        ServiceController<?> appAdminMount = this.serviceContainer.getService(applicationAdminResource("liveoak", "zero").append("mount"));
        assertThat(appAdminMount).isNotNull();
        assertThat(appAdminMount.getMode()).isEqualTo(Mode.ACTIVE);
        assertThat(appAdminMount.getState()).isEqualTo(State.UP); // we are mounted, but our parent isn't

        ServiceController<ApplicationContext> appContext = (ServiceController<ApplicationContext>) this.serviceContainer.getService(applicationContext("liveoak", "zero"));
        assertThat(appContext).isNotNull();
        assertThat(appContext.getMode()).isEqualTo(Mode.ACTIVE);
        assertThat(appContext.getState()).isEqualTo(State.UP);

        ServiceController<?> appContextMount = this.serviceContainer.getService(applicationContext("liveoak", "zero").append("mount"));
        assertThat(appContextMount).isNotNull();
        assertThat(appContextMount.getMode()).isEqualTo(Mode.ACTIVE);
        assertThat(appContextMount.getState()).isEqualTo(State.UP);

        assertThat(appContext.getValue().parent()).isSameAs(orgContext.getValue());

        Collection<Resource> registeredApps = orgAdmin.getValue().applicationsResource().members();
        assertThat(registeredApps).isNotNull();
        assertThat(registeredApps).hasSize(1);
        assertThat(((ApplicationResource) registeredApps.iterator().next()).application()).isSameAs(app.getValue());

        Collection<Resource> appResources = appContext.getValue().members();
        assertThat(appResources).isNotNull();
        assertThat(appResources).isEmpty();

        // now let's wire up the Zero app's /organizations container, and it should all fall together.

        OrganizationsResource orgs = new OrganizationsResource(null);

        this.serviceContainer.addService(applicationResource("liveoak", "zero", "organizations"), new ValueService<RootResource>(new ImmediateValue<>(orgs)))
                .install();

        MountService orgsMountService = new MountService();
        this.serviceContainer.addService(applicationResource("liveoak", "zero", "organizations").append("mount"), orgsMountService)
                .addDependency(applicationContext("liveoak", "zero"), MountPointResource.class, orgsMountService.mountPointInjector())
                .addDependency(applicationResource("liveoak", "zero", "organizations"), RootResource.class, orgsMountService.resourceInjector())
                .install();

        this.serviceContainer.awaitStability();

        appResources = appContext.getValue().members();
        assertThat(appResources).isNotNull();
        assertThat(appResources).hasSize(1);

        assertThat(orgs.members()).isNotNull();
        assertThat(orgs.members()).hasSize(1);

        OrganizationResource liveoakOrg = (OrganizationResource) orgs.members().iterator().next();
        assertThat(liveoakOrg).isNotNull();

        ApplicationsResource liveoakOrgApps = liveoakOrg.applicationsResource();
        assertThat(liveoakOrgApps).isNotNull();

        ApplicationResource zeroApp = (ApplicationResource) liveoakOrgApps.member("zero");
        assertThat(zeroApp).isNotNull();

        // now install global context, and public end should fall together.

        GlobalContext globalContext = new GlobalContext();
        this.serviceContainer.addService(GLOBAL_CONTEXT, new ValueService<GlobalContext>(new ImmediateValue<>(globalContext)))
                .install();

        this.serviceContainer.awaitStability();

        assertThat(globalContext.member("liveoak")).isSameAs(orgContext.getValue());
        assertThat(orgContext.getValue().parent()).isSameAs(globalContext);

        assertThat(installedOrg.context()).isSameAs(orgContext.getValue());
        assertThat(installedOrg.resource()).isSameAs(orgAdmin.getValue());

        assertThat(installedApp.context()).isSameAs(appContext.getValue());
        assertThat(installedApp.resource()).isSameAs(appAdmin.getValue());

    }

    @Test
    public void testBootstrapUsingExtension() throws Exception {
        InternalOrganizationRegistry registry = new InternalOrganizationRegistry(this.serviceContainer);
        this.serviceContainer.addListener(new DebugServiceListener());
        this.serviceContainer.addService(LiveOak.ORGANIZATION_REGISTRY, new ValueService<InternalOrganizationRegistry>(new ImmediateValue<>(registry)))
                .install();

        GlobalContext globalContext = new GlobalContext();
        this.serviceContainer.addService(LiveOak.GLOBAL_CONTEXT, new ValueService<GlobalContext>(new ImmediateValue<>(globalContext)))
                .install();

        ExtensionService ext = new ExtensionService("zero", new ZeroExtension(), JsonNodeFactory.instance.objectNode(), ServiceName.of( "admin-mount" ));
        this.serviceContainer.addService(LiveOak.extension("zero"), ext)
                .install();

        this.serviceContainer.awaitStability();

        ServiceController<InternalOrganization> org = (ServiceController<InternalOrganization>) this.serviceContainer.getService(organization("liveoak"));

        assertThat(org).isNotNull();
        assertThat(org.getMode()).isEqualTo(Mode.ACTIVE);
        assertThat(org.getState()).isEqualTo(State.UP);

        ServiceController<OrganizationResource> orgAdmin = (ServiceController<OrganizationResource>) this.serviceContainer.getService(organizationAdminResource("liveoak"));

        assertThat(orgAdmin).isNotNull();
        assertThat(orgAdmin.getMode()).isEqualTo(Mode.ACTIVE);
        assertThat(orgAdmin.getState()).isEqualTo(State.UP);

        ServiceController<?> orgAdminMount = this.serviceContainer.getService(LiveOak.organizationAdminResource("liveoak").append("mount"));
        assertThat(orgAdminMount).isNotNull();
        assertThat(orgAdminMount.getMode()).isEqualTo(Mode.ACTIVE);
        assertThat(orgAdminMount.getState()).isEqualTo(State.UP);

        ServiceController<OrganizationContext> orgContext = (ServiceController<OrganizationContext>) this.serviceContainer.getService(organizationContext("liveoak"));
        assertThat(orgContext).isNotNull();
        assertThat(orgContext.getMode()).isEqualTo(Mode.ACTIVE);
        assertThat(orgContext.getState()).isEqualTo(State.UP);

        ServiceController<?> orgContextMount = this.serviceContainer.getService(LiveOak.organizationContext("liveoak").append("mount"));
        assertThat(orgContextMount).isNotNull();
        assertThat(orgContextMount.getMode()).isEqualTo(Mode.ACTIVE);
        assertThat(orgContextMount.getState()).isEqualTo(State.UP);

        ServiceController<InternalApplication> app = (ServiceController<InternalApplication>) this.serviceContainer.getService(application("liveoak", "zero"));

        assertThat(app).isNotNull();
        assertThat(app.getMode()).isEqualTo(Mode.ACTIVE);
        assertThat(app.getState()).isEqualTo(State.UP);

        ServiceController<ApplicationResource> appAdmin = (ServiceController<ApplicationResource>) this.serviceContainer.getService(applicationAdminResource("liveoak", "zero"));
        assertThat(appAdmin).isNotNull();
        assertThat(appAdmin.getMode()).isEqualTo(Mode.ACTIVE);
        assertThat(appAdmin.getState()).isEqualTo(State.UP);

        ServiceController<?> appAdminMount = this.serviceContainer.getService(applicationAdminResource("liveoak", "zero").append("mount"));
        assertThat(appAdminMount).isNotNull();
        assertThat(appAdminMount.getMode()).isEqualTo(Mode.ACTIVE);
        assertThat(appAdminMount.getState()).isEqualTo(State.UP); // we are mounted, but our parent isn't

        ServiceController<ApplicationContext> appContext = (ServiceController<ApplicationContext>) this.serviceContainer.getService(applicationContext("liveoak", "zero"));
        assertThat(appContext).isNotNull();
        assertThat(appContext.getMode()).isEqualTo(Mode.ACTIVE);
        assertThat(appContext.getState()).isEqualTo(State.UP);

        ServiceController<?> appContextMount = this.serviceContainer.getService(applicationContext("liveoak", "zero").append("mount"));
        assertThat(appContextMount).isNotNull();
        assertThat(appContextMount.getMode()).isEqualTo(Mode.ACTIVE);
        assertThat(appContextMount.getState()).isEqualTo(State.UP);


        Collection<Resource> registeredApps = orgAdmin.getValue().applicationsResource().members();
        assertThat(registeredApps).isNotNull();
        assertThat(registeredApps).hasSize(1);
        assertThat(((ApplicationResource) registeredApps.iterator().next()).application()).isSameAs(app.getValue());

        Collection<Resource> appResources = appContext.getValue().members();
        assertThat(appResources).isNotNull();
        assertThat(appResources).hasSize(2);

        OrganizationsResource orgs = (OrganizationsResource) appContext.getValue().member("organizations");

        assertThat(orgs.members()).isNotNull();
        assertThat(orgs.members()).hasSize(1);

        OrganizationResource liveoakOrg = (OrganizationResource) orgs.members().iterator().next();
        assertThat(liveoakOrg).isNotNull();

        ApplicationsResource liveoakOrgApps = liveoakOrg.applicationsResource();
        assertThat(liveoakOrgApps).isNotNull();

        ApplicationResource zeroApp = (ApplicationResource) liveoakOrgApps.member("zero");
        assertThat(zeroApp).isNotNull();

        ApplicationExtensionsResource extsResource = zeroApp.extensionsResource();
        assertThat(extsResource).isNotNull();
    }

}
