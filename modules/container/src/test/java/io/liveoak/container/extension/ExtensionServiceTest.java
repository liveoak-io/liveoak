package io.liveoak.container.extension;

import java.util.Properties;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.liveoak.common.util.ObjectMapperFactory;
import io.liveoak.container.service.ClientService;
import io.liveoak.container.tenancy.ExtensionConfigurationManager;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.Services;
import org.jboss.msc.service.ServiceContainer;
import org.jboss.msc.service.ServiceController;
import org.jboss.msc.service.ValueService;
import org.jboss.msc.value.ImmediateValue;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 */
public class ExtensionServiceTest {

    private ServiceContainer serviceContainer;

    @Before
    public void setUpServiceContainer() throws Exception {
        this.serviceContainer = ServiceContainer.Factory.create();

        ExtensionService mockExt = new ExtensionService("mock", new MockExtension(), getConfig(), null);
        this.serviceContainer.addService(Services.extension("mock"), mockExt).install();
        this.serviceContainer.addService(Services.CLIENT, new ClientService()).install();
        this.serviceContainer.addService(Services.systemConfigurationManager("mock"), new ValueService<>(new ImmediateValue<>(new ExtensionConfigurationManager("mock", null)))).install();
        this.serviceContainer.addService(Services.systemEnvironmentProperties("mock"), new ValueService<>(new ImmediateValue<>(new Properties(System.getProperties())))).install();
        serviceContainer.awaitStability();
    }

    private ObjectNode getConfig() throws Exception {
        JsonNode instancesNode = ObjectMapperFactory.create().readTree(
                        "{\n" +
                        "    module-id:  'io.liveoak.mock',\n" +
                        "    config: {\n" +
                        "      message: 'hello mock'\n" +
                        "    },\n" +
                        "    instances: {\n" +
                        "      'foo' : {message: 'hello foo'},\n" +
                        "      'bar' : {message: 'hello bar'}\n" +
                        "    }\n" +
                        "}\n");
        return (ObjectNode)instancesNode;
    }

    @After
    public void tearDownServiceContainer() {
        this.serviceContainer.shutdown();
    }

    @Test
    public void testRead() throws Exception {

        ServiceController<?> moduleResourceServiceController = this.serviceContainer.getService(MockExtension.adminResource( "module" ));

        assertThat( moduleResourceServiceController ).isNotNull();
        assertThat( moduleResourceServiceController.getValue() ).isNotNull();
        assertThat( moduleResourceServiceController.getValue() ).isInstanceOf(MockAdminResource.class);
        MockAdminResource moduleResource = (MockAdminResource)moduleResourceServiceController.getValue();
        assertThat(moduleResource.flavor()).isEqualTo( "system" );
        assertThat(moduleResource.properties(new RequestContext.Builder()).get("message")).isEqualTo("hello mock");

        ServiceController<?> fooResourceServiceController = this.serviceContainer.getService(MockExtension.adminResource( "foo" ));

        assertThat( fooResourceServiceController ).isNotNull();
        assertThat( fooResourceServiceController.getValue() ).isNotNull();
        assertThat( fooResourceServiceController.getValue() ).isInstanceOf(MockAdminResource.class);
        MockAdminResource fooResource = (MockAdminResource)fooResourceServiceController.getValue();
        assertThat(fooResource.flavor()).isEqualTo( "instance" );
        assertThat(fooResource.properties(new RequestContext.Builder()).get("message")).isEqualTo("hello foo");

        ServiceController<?> barResourceServiceController = this.serviceContainer.getService(MockExtension.adminResource( "bar" ));

        assertThat( barResourceServiceController ).isNotNull();
        assertThat( barResourceServiceController.getValue() ).isNotNull();
        assertThat( barResourceServiceController.getValue() ).isInstanceOf(MockAdminResource.class);
        MockAdminResource barResource = (MockAdminResource)barResourceServiceController.getValue();
        assertThat(barResource.flavor()).isEqualTo( "instance" );
        assertThat(barResource.properties(new RequestContext.Builder()).get("message")).isEqualTo("hello bar");


    }

}
