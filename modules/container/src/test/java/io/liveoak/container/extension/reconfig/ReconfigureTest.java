package io.liveoak.container.extension.reconfig;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.liveoak.common.codec.DefaultResourceState;
import io.liveoak.container.extension.ExtensionConfigResource;
import io.liveoak.container.extension.ExtensionInstaller;
import io.liveoak.container.extension.MockExtension;
import io.liveoak.container.extension.MockResource;
import io.liveoak.container.tenancy.MountPointResource;
import io.liveoak.container.tenancy.SimpleResourceRegistry;
import io.liveoak.spi.LiveOak;
import io.liveoak.spi.resource.async.Resource;
import io.liveoak.spi.state.ResourceState;
import org.jboss.msc.service.*;
import org.jboss.msc.value.ImmediateValue;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;

/**
 * @author Bob McWhirter
 */
public class ReconfigureTest {

    private ServiceContainer serviceContainer;
    private SimpleResourceRegistry systemAdminMount;


    @Before
    public void setUpServiceContainer() {
        this.serviceContainer = ServiceContainer.Factory.create();
        this.systemAdminMount = new SimpleResourceRegistry("admin");
        this.serviceContainer.addService(ServiceName.of("admin-mount"), new ValueService<MountPointResource>(new ImmediateValue<>(this.systemAdminMount)))
                .install();
        this.serviceContainer.addService(LiveOak.SERVICE_CONTAINER, new ValueService<ServiceContainer>(new ImmediateValue<>(this.serviceContainer)))
                .install();
    }

    @After
    public void tearDownServiceContainer() {
        this.serviceContainer.shutdown();
    }

    @Test
    public void testReconfigureSystem() throws Exception {
        ExtensionInstaller loader = new ExtensionInstaller(this.serviceContainer, ServiceName.of("admin-mount"));
        loader.load("reconfig", new ReconfigurableExtension(), JsonNodeFactory.instance.objectNode());

        this.serviceContainer.awaitStability();

        MockConsumerService consumer = new MockConsumerService();

        this.serviceContainer.addService(ServiceName.of("consume"), consumer)
                .addDependency(ReconfigurableExtension.mainClient("reconfig"), MockClient.class, consumer.clientInjector())
                .install();

        ServiceController<Resource> adminResource = (ServiceController<Resource>) this.serviceContainer.getService(LiveOak.extension("reconfig").append("admin"));


        assertThat(adminResource).isNotNull();
        assertThat(adminResource.getValue()).isNotNull();
        assertThat(adminResource.getValue()).isInstanceOf(ExtensionConfigResource.class);

        assertThat(this.systemAdminMount.member("reconfig")).isSameAs(adminResource.getValue());

        ResourceState newState = new DefaultResourceState();
        newState.putProperty("host", "host2");
        newState.putProperty("port", 2);

        MockResponder responder = new MockResponder();
        adminResource.getValue().updateProperties(null, newState, responder);
        responder.await();

        this.serviceContainer.awaitStability();

        ServiceController<MockClient> clientController = (ServiceController<MockClient>) this.serviceContainer.getService(ReconfigurableExtension.mainClient("reconfig"));
        assertThat(clientController).isNotNull();
        assertThat(clientController.getValue()).isNotNull();

        ObjectNode config = clientController.getValue().config();

        assertThat(config.get("host").asText()).isEqualTo("host2");
        assertThat(config.get("port").asInt()).isEqualTo(2);

        assertThat(consumer.getValue()).isNotNull();
        assertThat(consumer.getValue()).isSameAs(clientController.getValue());

    }
}
