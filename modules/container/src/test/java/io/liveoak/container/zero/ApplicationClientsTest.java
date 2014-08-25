package io.liveoak.container.zero;

import java.io.File;
import java.util.List;

import io.liveoak.common.codec.DefaultResourceState;
import io.liveoak.container.LiveOakFactory;
import io.liveoak.container.LiveOakSystem;
import io.liveoak.container.tenancy.InternalApplication;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.client.Client;
import io.liveoak.spi.state.ResourceState;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;

/**
 * @author Ken Finnigan
 */
public class ApplicationClientsTest {

    private LiveOakSystem system;
    private Client client;
    private InternalApplication application;

    @Before
    public void setUp() throws Exception {
        this.system = LiveOakFactory.create(null, new File(getClass().getClassLoader().getResource("apps").getFile()), null);
        this.client = this.system.client();

        // LIVEOAK-295 ... make sure system services have all started before performing programmatic application deployment
        this.system.awaitStability();

        this.application = this.system.applicationRegistry().createApplication("testApp", "Test Application");

        this.system.awaitStability();
    }

    @After
    public void shutdown() {
        this.system.stop();
    }

    @AfterClass
    public static void cleanUpInstalledApps() throws Exception {
        new AppCleanup().accept("apps/testApp");
    }

    @Test
    public void testClientReadAndStore() throws Throwable {
        // Read the current clients, should be empty
        RequestContext requestContext = new RequestContext.Builder().build();
        ResourceState testAppConfig = this.client.read(requestContext, "admin/applications/testApp/resources/application-clients");

        assertThat(testAppConfig).isNotNull();
        assertThat(testAppConfig.getProperty("members")).isNull();

        // Store a client
        requestContext = new RequestContext.Builder().build();
        ResourceState htmlClientState = new DefaultResourceState();
        htmlClientState.putProperty("id", "my-html-client");
        htmlClientState.putProperty("type", "HTML");
        htmlClientState.putProperty("security-key", "html-client");

        testAppConfig = this.client.create(requestContext, "/admin/applications/testApp/resources/application-clients", htmlClientState);
        assertThat(testAppConfig).isNotNull();

        assertThat(testAppConfig.id()).isEqualTo("my-html-client");
        assertThat(testAppConfig.getProperty("type")).isEqualTo("HTML");
        assertThat(testAppConfig.getProperty("security-key")).isEqualTo("html-client");

        requestContext = new RequestContext.Builder().build();
        testAppConfig = this.client.read(requestContext, "admin/applications/testApp/resources/application-clients");

        assertThat(testAppConfig).isNotNull();
        List<ResourceState> clients = testAppConfig.members();
        assertThat(clients).isNotNull();
        assertThat(clients.size()).isEqualTo(1);

        ResourceState client = clients.get(0);
        assertThat(client.id()).isEqualTo("my-html-client");

        requestContext = new RequestContext.Builder().build();
        testAppConfig = this.client.read(requestContext, "admin/applications/testApp/resources/application-clients/my-html-client");

        assertThat(testAppConfig).isNotNull();
        assertThat(testAppConfig.id()).isEqualTo("my-html-client");
        assertThat(testAppConfig.getProperty("type")).isEqualTo("HTML");
        assertThat(testAppConfig.getProperty("security-key")).isEqualTo("html-client");
    }
}

