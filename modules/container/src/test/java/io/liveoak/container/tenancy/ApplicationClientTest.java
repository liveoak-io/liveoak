package io.liveoak.container.tenancy;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Map;

import io.liveoak.common.codec.DefaultResourceState;
import io.liveoak.container.LiveOakFactory;
import io.liveoak.container.LiveOakSystem;
import io.liveoak.spi.ApplicationClient;
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
public class ApplicationClientTest {

    private LiveOakSystem system;
    private Client client;
    private InternalApplication application;

    @Before
    public void setUp() throws Exception {
        this.system = LiveOakFactory.create(null, new File(getClass().getClassLoader().getResource("apps").getFile()), null);
        this.client = this.system.client();

        // LIVEOAK-295 ... make sure system services have all started before performing programmatic application deployment
        this.system.awaitStability();

        this.application = this.system.applicationRegistry().createApplication( "testApp", "Test Application" );

        this.system.awaitStability();
    }

    @After
    public void shutdown() {
        this.system.stop();
    }

    @AfterClass
    public static void cleanUpInstalledApps() throws Exception {
        File myApp = new File(ApplicationClientTest.class.getClassLoader().getResource("apps/testApp").getFile());
        if (myApp != null && myApp.exists()) {
            deleteNonEmptyDir(myApp);
        }
    }

    private static void deleteNonEmptyDir(File dir) throws IOException {
        Path directory = dir.toPath();
        Files.walkFileTree(directory, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.delete(file);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                Files.delete(dir);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    @Test
    public void testClientReadAndStore() throws Throwable {
        // Read the current clients, should be empty
        RequestContext requestContext = new RequestContext.Builder().build();
        ResourceState testAppConfig = this.client.read(requestContext, "admin/applications/testApp");

        assertThat(testAppConfig).isNotNull();
        Map<String, ApplicationClient> clients = (Map<String, ApplicationClient>) testAppConfig.getProperty("clients");
        assertThat(clients).isNull();

        // Store a client
        requestContext = new RequestContext.Builder().build();
        ResourceState clientState = new DefaultResourceState();
        ResourceState htmlClientState = new DefaultResourceState();
        htmlClientState.putProperty("type", "HTML");
        htmlClientState.putProperty("security-key", "html-client");
        clientState.putProperty("html-client", htmlClientState);
        testAppConfig.putProperty("clients", clientState);

        testAppConfig = this.client.update(requestContext, "/admin/applications/testApp", testAppConfig);
        assertThat(testAppConfig).isNotNull();

        clients = (Map<String, ApplicationClient>) testAppConfig.getProperty("clients");
        assertThat(clients).isNotNull();
        assertThat(clients.size()).isEqualTo(1);
        assertThat(clients.get("html-client")).isNotNull();
        assertThat(clients.get("html-client").id()).isEqualTo("html-client");
        assertThat(clients.get("html-client").type()).isEqualTo("HTML");
        assertThat(clients.get("html-client").securityKey()).isEqualTo("html-client");
    }
}
