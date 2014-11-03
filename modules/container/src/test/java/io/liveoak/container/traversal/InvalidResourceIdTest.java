package io.liveoak.container.traversal;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import io.liveoak.container.AbstractContainerTest;
import io.liveoak.container.LiveOakFactory;
import io.liveoak.container.tenancy.InternalApplication;
import io.liveoak.spi.MediaType;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.extension.ApplicationExtensionContext;
import io.liveoak.spi.extension.Extension;
import io.liveoak.spi.extension.SystemExtensionContext;
import io.liveoak.spi.resource.RootResource;
import io.liveoak.spi.resource.SynchronousResource;
import io.liveoak.spi.resource.async.Resource;
import io.liveoak.spi.resource.async.Responder;
import io.liveoak.spi.state.ResourceState;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.ServiceTarget;
import org.jboss.msc.service.ValueService;
import org.jboss.msc.value.ImmediateValue;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;

/**
 * @author Ken Finnigan
 */
public class InvalidResourceIdTest extends AbstractContainerTest {

    private CloseableHttpClient httpClient;
    private static InternalApplication application;

    @BeforeClass
    public static void setUpServer() throws Exception {
        system = LiveOakFactory.create();
        system.extensionInstaller().load("dummy", new BadResourceExtension());
        awaitStability();

        application = system.applicationRegistry().createApplication("testApp", "Test Application");
        application.extend("dummy", "idtest", JsonNodeFactory.instance.objectNode());
        awaitStability();
    }

    @AfterClass
    public static void tearDownServer() throws Exception {
        system.stop();
    }

    @Before
    public void setUpClient() throws Exception {
        this.httpClient = HttpClientBuilder.create().build();
    }

    @After
    public void tearDownClient() throws Exception {
        this.httpClient.close();
    }

    @Test
    public void testNullResourceId() throws Exception {
        HttpPost post = new HttpPost("http://localhost:8080/testApp/idtest");
        post.setEntity(new StringEntity("{ \"test-type\": \"null\" }"));
        post.setHeader("Content-Type", MediaType.JSON.toString());
        CloseableHttpResponse response = httpClient.execute(post);
        assertThat(response.getStatusLine().getStatusCode()).isEqualTo(500);
        response.close();
    }

    @Test
    public void testEmptyResourceId() throws Exception {
        HttpPost post = new HttpPost("http://localhost:8080/testApp/idtest");
        post.setEntity(new StringEntity("{ \"test-type\": \"empty\" }"));
        post.setHeader("Content-Type", MediaType.JSON.toString());
        CloseableHttpResponse response = httpClient.execute(post);
        assertThat(response.getStatusLine().getStatusCode()).isEqualTo(500);
        response.close();
    }

    @Test
    public void testValidResourceId() throws Exception {
        HttpPost post = new HttpPost("http://localhost:8080/testApp/idtest");
        CloseableHttpResponse response = httpClient.execute(post);
        assertThat(response.getStatusLine().getStatusCode()).isEqualTo(201);
        response.close();
    }

    private static class BadResourceExtension implements Extension {

        static ServiceName resource(String appId, String id) {
            return ServiceName.of("bad", "resource", appId, id);
        }

        @Override
        public void extend(SystemExtensionContext context) throws Exception {
        }

        @Override
        public void extend(ApplicationExtensionContext context) throws Exception {
            String appId = context.application().id();
            BadRootResource resource = new BadRootResource(context.resourceId());

            ServiceTarget target = context.target();
            target.addService(resource(appId, context.resourceId()), new ValueService<>(new ImmediateValue<>(resource)))
                    .install();

            context.mountPublic(resource(appId, context.resourceId()));
        }

        @Override
        public void unextend(ApplicationExtensionContext context) throws Exception {
        }
    }

    private static class BadRootResource implements RootResource, SynchronousResource {
        private String id;
        private Resource parent;

        public BadRootResource(String id) {
            this.id = id;
        }

        @Override
        public void parent(Resource parent) {
            this.parent = parent;
        }

        @Override
        public Resource parent() {
            return this.parent;
        }

        @Override
        public String id() {
            return this.id;
        }

        @Override
        public void createMember(RequestContext ctx, ResourceState state, Responder responder) throws Exception {
            String type = state.getPropertyAsString("test-type");
            if (type != null) {
                if (type.equals("null")) {
                    responder.resourceCreated(new BadResource(null, this));
                } else if (type.equals("empty")) {
                    responder.resourceCreated(new BadResource("", this));
                }
            } else {
                responder.resourceCreated(new BadResource("fine", this));
            }
        }
    }

    private static class BadResource implements SynchronousResource {
        String id;
        Resource parent;

        public BadResource(String id, Resource parent) {
            this.id = id;
            this.parent = parent;
        }

        @Override
        public Resource parent() {
            return this.parent;
        }

        @Override
        public String id() {
            return this.id;
        }
    }
}
