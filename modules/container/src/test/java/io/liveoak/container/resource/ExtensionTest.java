package io.liveoak.container.resource;

import io.liveoak.container.AbstractContainerTest;
import io.liveoak.container.LiveOakFactory;
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
public class ExtensionTest extends AbstractContainerTest {
    private Client client;


    @Before
    public void setUp() throws Exception {
        system = LiveOakFactory.create();
        this.client = system.client();
        awaitStability();

        InternalApplication application = system.applicationRegistry().createApplication("testApp", "Test Application");
        system.extensionInstaller().load("mock", new MockExtension());
        awaitStability();
        application.extend("mock");

        system.service(MockExtension.resource("testApp", "mock"));
    }

    @After
    public void shutdown() throws Exception {
        system.stop();
    }

    @Test
    public void testApplication() throws Throwable {
        RequestContext requestContext = new RequestContext.Builder().build();
        ResourceState result = this.client.read(requestContext, "/testApp/mock");

        assertThat(result.getProperty("application.name")).isEqualTo("Test Application");
        assertThat(result.getProperty("application.id")).isEqualTo("testApp");
    }


}
