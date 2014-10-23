package io.liveoak.container.resource;

import io.liveoak.container.LiveOakFactory;
import io.liveoak.container.LiveOakSystem;
import io.liveoak.container.tenancy.InternalApplication;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.client.Client;
import io.liveoak.spi.state.ResourceState;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 */
public class ExtensionTest {
    private LiveOakSystem system;
    private Client client;
    private InternalApplication application;


    @Before
    public void setUp() throws Exception {
        this.system = LiveOakFactory.create();
        this.client = this.system.client();

        // LIVEOAK-295 ... make sure system services have all started before performing programmatic application deployment
        this.system.awaitStability();

        this.application = this.system.applicationRegistry().createApplication("testApp", "Test Application");
        this.system.extensionInstaller().load("mock", new MockExtension());
        this.application.extend("mock");

        this.system.service(MockExtension.resource("testApp", "mock"));
    }

    @After
    public void shutdown() throws Exception {
        this.system.stop();
    }

    @Test
    public void testApplication() throws Throwable {
        RequestContext requestContext = new RequestContext.Builder().build();
        ResourceState result = this.client.read(requestContext, "/testApp/mock");

        assertThat(result.getProperty("application.name")).isEqualTo("Test Application");
        assertThat(result.getProperty("application.id")).isEqualTo("testApp");
    }


}
