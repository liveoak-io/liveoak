package io.liveoak.git;

import java.io.File;
import java.util.concurrent.CompletableFuture;

import io.liveoak.common.codec.DefaultResourceState;
import io.liveoak.common.security.DefaultSecurityContext;
import io.liveoak.git.extension.GitExtension;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.client.ClientResourceResponse;
import io.liveoak.spi.state.ResourceState;
import io.liveoak.testtools.AbstractTestCase;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;

/**
 * @author Ken Finnigan
 */
public class ConcurrentAccessTest extends AbstractTestCase {

    @BeforeClass
    public static void setup() throws Exception {
        loadExtension("git", new GitExtension());
        installTestApp();
    }

    @Test
    public void concurrentCommits() throws Exception {
        ResourceState commit1 = new DefaultResourceState();
        commit1.putProperty("msg", "Concurrent commit 1");

        ResourceState commit2 = new DefaultResourceState();
        commit2.putProperty("msg", "Concurrent commit 2");

        File temp = new File(testApplication.directory(), "temp.txt");
        assertThat(temp.createNewFile()).isTrue();

        CompletableFuture<ClientResourceResponse> response1 = new CompletableFuture<>();
        CompletableFuture<ClientResourceResponse> response2 = new CompletableFuture<>();

        client.create(new RequestContext.Builder().securityContext(new DefaultSecurityContext()).build(), "/admin/applications/testApp/resources/git/commits", commit1, response -> {
            response1.complete(response);
        });

        client.create(new RequestContext.Builder().securityContext(new DefaultSecurityContext()).build(), "/admin/applications/testApp/resources/git/commits", commit2, response -> {
            response2.complete(response);
        });

        // Check first result
        ClientResourceResponse response = response1.get();
        assertThat(response.responseType()).isEqualTo(ClientResourceResponse.ResponseType.OK);
        assertThat(response.state()).isNotNull();
        assertThat(response.state().id()).isNotNull();

        // Check second result
        response = response2.get();
        assertThat(response.responseType()).isEqualTo(ClientResourceResponse.ResponseType.OK);
        assertThat(response.state()).isNotNull();
        assertThat(response.state().id()).isNotNull();
    }
}
