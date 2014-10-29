package io.liveoak.container.codec;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import io.liveoak.common.DefaultReturnFields;
import io.liveoak.container.AbstractContainerTest;
import io.liveoak.container.LiveOakFactory;
import io.liveoak.container.tenancy.InternalApplication;
import io.liveoak.spi.LiveOak;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.ReturnFields;
import io.liveoak.spi.client.Client;
import io.liveoak.spi.extension.ApplicationExtensionContext;
import io.liveoak.spi.extension.Extension;
import io.liveoak.spi.extension.SystemExtensionContext;
import io.liveoak.spi.resource.RootResource;
import io.liveoak.spi.resource.async.Resource;
import io.liveoak.spi.resource.async.ResourceSink;
import io.liveoak.spi.state.ResourceState;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 * @author Ken Finnigan
 */
public class MemberEncodingDriverTest extends AbstractContainerTest {

    private static Client client;

    private static final String PATH = "/testApp/";
    private static final String RESOURCE_ID = "readMembers";

    static TestReadingMembersResource resource;

    @BeforeClass
    public static void setUp() throws Exception {
        system = LiveOakFactory.create();
        client = system.client();
        system.extensionInstaller().load(RESOURCE_ID, new TestReadingMembersExtension());
        awaitStability();

        InternalApplication application = system.applicationRegistry().createApplication("testApp", "Test Application");
        application.extend(RESOURCE_ID, RESOURCE_ID, JsonNodeFactory.instance.objectNode());
        awaitStability();
    }

    @AfterClass
    public static void shutdown() {
        system.stop();
    }

    @After
    public void resetResourceFlag() {
        resource.resetFlag();
    }

    @Test
    public void testReadWithNoReturnFields() throws Exception {
        // by default the members are returned
        ResourceState configState = client.read(new RequestContext.Builder().build(), PATH + RESOURCE_ID);

        assertThat(configState).isNotNull();
        assertThat(configState.id()).isEqualTo(RESOURCE_ID);
        assertThat(resource.getReadMembers()).isTrue();
    }

    @Test
    public void testReadWithAllReturnFields() throws Exception {
        // when all is specified, the members should be returned
        ResourceState configState = client.read(new RequestContext.Builder().returnFields(ReturnFields.ALL).build(), PATH + RESOURCE_ID);

        assertThat(configState).isNotNull();
        assertThat(configState.id()).isEqualTo(RESOURCE_ID);
        assertThat(resource.getReadMembers()).isTrue();
    }

    @Test
    public void testReadWithAsteriskReturnFields() throws Exception {
        // when all is specified, the members should be returned
        ResourceState configState = client.read(new RequestContext.Builder().returnFields(new DefaultReturnFields("*")).build(), PATH + RESOURCE_ID);

        assertThat(configState).isNotNull();
        assertThat(configState.id()).isEqualTo(RESOURCE_ID);
        assertThat(resource.getReadMembers()).isTrue();
    }

    @Test
    public void testReadWithNoneReturnFields() throws Exception {
        // when requesting no return fields, we should not get the members
        ResourceState configState = client.read(new RequestContext.Builder().returnFields(ReturnFields.NONE).build(), PATH + RESOURCE_ID);

        assertThat(configState).isNotNull();
        assertThat(configState.id()).isEqualTo(RESOURCE_ID);
        assertThat(resource.getReadMembers()).isFalse();
    }

    @Test
    public void testReadWithOtherReturnFields() throws Exception {
        // when requesting return fields and not members, should not be reading the members
        ResourceState configState = client.read(new RequestContext.Builder().returnFields(new DefaultReturnFields("foo")).build(), PATH + RESOURCE_ID);

        assertThat(configState).isNotNull();
        assertThat(configState.id()).isEqualTo(RESOURCE_ID);
        assertThat(resource.getReadMembers()).isFalse();
    }

    @Test
    public void testReadWithMembersReturnFields() throws Exception {
        // when requesting 'members' as part of the return fields, should be reading the members
        ResourceState configState = client.read(new RequestContext.Builder().returnFields(new DefaultReturnFields(LiveOak.MEMBERS)).build(), PATH + RESOURCE_ID);

        assertThat(configState).isNotNull();
        assertThat(configState.id()).isEqualTo(RESOURCE_ID);
        assertThat(resource.getReadMembers()).isTrue();
    }

    @Test
    public void testReadWithMembersAndOtherReturnFields() throws Exception {
        // when requesting 'members' as part of the return fields, should be reading the members
        ResourceState configState = client.read(new RequestContext.Builder().returnFields(new DefaultReturnFields("foo,members")).build(), PATH + RESOURCE_ID);

        assertThat(configState).isNotNull();
        assertThat(configState.id()).isEqualTo(RESOURCE_ID);
        assertThat(resource.getReadMembers()).isTrue();
    }

    @Test
    public void testReadWithMultipleReturnFields() throws Exception {
        // when requesting 'members' as part of the return fields, should be reading the members
        ResourceState configState = client.read(new RequestContext.Builder().returnFields(new DefaultReturnFields("bar,foo, members")).build(), PATH + RESOURCE_ID);

        assertThat(configState).isNotNull();
        assertThat(configState.id()).isEqualTo(RESOURCE_ID);
        assertThat(resource.getReadMembers()).isFalse();
    }

    private static class TestReadingMembersExtension implements Extension {

        @Override
        public void extend(SystemExtensionContext context) throws Exception {
            // do nothing
        }

        @Override
        public void extend(ApplicationExtensionContext context) throws Exception {
            resource = new TestReadingMembersResource(context.resourceId());
            context.mountPublic(resource);
        }

        @Override
        public void unextend(ApplicationExtensionContext context) throws Exception {
            //do nothing
        }
    }

    private static class TestReadingMembersResource implements RootResource {

        Resource parent;
        String id;
        boolean readMembers = false;

        public void resetFlag() {
            readMembers = false;
        }

        public TestReadingMembersResource(String id) {
            this.id = id;
        }

        public boolean getReadMembers() {
            return this.readMembers;
        }

        @Override
        public void parent(Resource parent) {
            this.parent = parent;
        }

        @Override
        public Resource parent() {
            return parent;
        }

        @Override
        public String id() {
            return id;
        }

        @Override
        public void readMembers(RequestContext ctx, ResourceSink sink) throws Exception {
            readMembers = true;
            sink.complete();
        }
    }

}
