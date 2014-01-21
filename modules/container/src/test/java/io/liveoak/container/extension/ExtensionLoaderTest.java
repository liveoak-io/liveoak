package io.liveoak.container.extension;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import io.liveoak.container.tenancy.MountPointResource;
import io.liveoak.container.tenancy.SimpleResourceRegistry;
import io.liveoak.spi.LiveOak;
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
    private SimpleResourceRegistry systemAdminMount;

    @Before
    public void setUpServiceContainer() {
        this.serviceContainer = ServiceContainer.Factory.create();
        this.systemAdminMount = new SimpleResourceRegistry("admin" );
        this.serviceContainer.addService( ServiceName.of("admin-mount"), new ValueService<MountPointResource>( new ImmediateValue<>( this.systemAdminMount )))
                .install();
        this.serviceContainer.addService( LiveOak.SERVICE_CONTAINER, new ValueService<ServiceContainer>( new ImmediateValue<>( this.serviceContainer ) ) )
                .install();
    }

    @After
    public void tearDownServiceContainer() {
        this.serviceContainer.shutdown();
    }

    @Test
    public void testLoadExtensionByInstance() throws Exception {
        ExtensionInstaller loader = new ExtensionInstaller( this.serviceContainer, ServiceName.of( "admin-mount" ) );
        loader.load( "mock", new MockExtension(), JsonNodeFactory.instance.objectNode() );

        this.serviceContainer.awaitStability();

        ServiceController<?> adminResource = this.serviceContainer.getService(LiveOak.extension( "mock" ).append( "admin" ) );

        assertThat( adminResource ).isNotNull();
        assertThat( adminResource.getValue() ).isNotNull();
        assertThat( adminResource.getValue() ).isInstanceOf( ExtensionConfigResource.class );

        assertThat( this.systemAdminMount.member( "mock" ) ).isSameAs( adminResource.getValue() );
    }
}

