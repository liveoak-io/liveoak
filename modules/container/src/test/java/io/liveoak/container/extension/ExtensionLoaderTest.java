package io.liveoak.container.extension;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import io.liveoak.common.DefaultMountPointResource;
import io.liveoak.container.service.ClientService;
import io.liveoak.container.zero.extension.ZeroExtension;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.Services;
import io.liveoak.spi.resource.DelegatingRootResource;
import io.liveoak.spi.resource.MountPointResource;
import io.liveoak.spi.resource.SynchronousResource;
import io.liveoak.spi.resource.async.Resource;
import org.jboss.msc.service.ServiceContainer;
import org.jboss.msc.service.ServiceController;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.ValueService;
import org.jboss.msc.value.ImmediateValue;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;

/**
 * @author Bob McWhirter
 */
public class ExtensionLoaderTest {

    private ServiceContainer serviceContainer;
    private DefaultMountPointResource systemAdminMount;

    @Before
    public void setUpServiceContainer() throws Exception {
        this.serviceContainer = ServiceContainer.Factory.create();
        this.systemAdminMount = new DefaultMountPointResource("admin");
        this.serviceContainer.addService(Services.resource(ZeroExtension.APPLICATION_ID, "system"), new ValueService<MountPointResource>(new ImmediateValue<>(this.systemAdminMount)))
                .install();
        this.serviceContainer.addService(Services.SERVICE_CONTAINER, new ValueService<ServiceContainer>(new ImmediateValue<>(this.serviceContainer)))
                .install();
        this.serviceContainer.addService(Services.CLIENT, new ClientService()).install();
        serviceContainer.awaitStability();
    }

    @After
    public void tearDownServiceContainer() {
        this.serviceContainer.shutdown();
    }

    @Test
    public void testLoadExtensionByInstance() throws Exception {
        ExtensionInstaller loader = new ExtensionInstaller(this.serviceContainer, ServiceName.of("admin-mount"));
        loader.load("mock", new MockExtension(), JsonNodeFactory.instance.objectNode());

        this.serviceContainer.awaitStability();

        ServiceController<?> adminResource = this.serviceContainer.getService(MockExtension.adminResource("module"));

        assertThat(adminResource).isNotNull();
        assertThat(adminResource.getValue()).isNotNull();
        assertThat(adminResource.getValue()).isInstanceOf(MockAdminResource.class);
        assertThat(((MockAdminResource) adminResource.getValue()).flavor()).isEqualTo("system");

        Resource adminMountResource = this.systemAdminMount.member(new RequestContext.Builder().build(), "mock");

        DelegatingRootResource delegatingResource = (DelegatingRootResource)((SynchronousResource) adminMountResource).member(new RequestContext.Builder().build(), "module");

        assertThat(delegatingResource.delegate()).isSameAs(adminResource.getValue());
    }
}

