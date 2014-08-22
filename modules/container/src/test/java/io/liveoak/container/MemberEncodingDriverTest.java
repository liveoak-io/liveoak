package io.liveoak.container;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import io.liveoak.common.DefaultReturnFields;
import io.liveoak.container.tenancy.InternalApplication;
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
import org.junit.Before;
import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 */
public class MemberEncodingDriverTest {

    private LiveOakSystem system;
    private Client client;
    private InternalApplication application;

    private static final String PATH = "/testApp/";
    private static final String RESOURCE_ID = "readMembers";


    TestReadingMembersResource resource;

    @Before
    public void setUp() throws Exception {
        this.system = LiveOakFactory.create();
        this.client = this.system.client();
        this.system.extensionInstaller().load(RESOURCE_ID, new TestReadingMembersExtension());

        // LIVEOAK-295 ... make sure system services have all started before performing programmatic application deployment
        this.system.awaitStability();

        this.application = this.system.applicationRegistry().createApplication( "testApp", "Test Application" );
        this.application.extend(RESOURCE_ID, RESOURCE_ID, JsonNodeFactory.instance.objectNode());
        this.system.awaitStability();
    }

    @After
    public void shutdown() {
        this.system.stop();
    }

    @Test
    public void testReadNoReturnFields() throws Exception {
        // by default the members are returned
        ResourceState configState = this.client.read(new RequestContext.Builder().build(), PATH + RESOURCE_ID);

        assertThat(configState).isNotNull();
        assertThat(configState.id()).isEqualTo(RESOURCE_ID);

        assertThat(resource.getReadMembers()).isTrue();
    }

    @Test
    public void testReadAllReturnFields() throws Exception {
        // when all is specified, the members should be returned
        ResourceState configState = this.client.read(new RequestContext.Builder().returnFields( ReturnFields.ALL).build(), PATH + RESOURCE_ID);

        assertThat(configState).isNotNull();
        assertThat(configState.id()).isEqualTo(RESOURCE_ID);

        assertThat(resource.getReadMembers()).isTrue();
    }

    @Test
    public void testReadAsteriskReturnFields() throws Exception {
        // when all is specified, the members should be returned
        ResourceState configState = this.client.read(new RequestContext.Builder().returnFields(new DefaultReturnFields("*")).build(), PATH + RESOURCE_ID);

        assertThat(configState).isNotNull();
        assertThat(configState.id()).isEqualTo(RESOURCE_ID);

        assertThat(resource.getReadMembers()).isTrue();
    }

    @Test
    public void testReadNoneReturnFields() throws Exception {
        // when requesting no return fields, we should not get the members
        ResourceState configState = this.client.read(new RequestContext.Builder().returnFields( ReturnFields.NONE).build(), PATH + RESOURCE_ID);

        assertThat(configState).isNotNull();
        assertThat(configState.id()).isEqualTo(RESOURCE_ID);

        assertThat(resource.getReadMembers()).isFalse();
    }

    @Test
    public void testReadOtherReturnFields() throws Exception {
        // when requesting return fields and not members, should not be reading the members
        ResourceState configState = this.client.read(new RequestContext.Builder().returnFields( new DefaultReturnFields("foo")).build(), PATH + RESOURCE_ID);

        assertThat(configState).isNotNull();
        assertThat(configState.id()).isEqualTo(RESOURCE_ID);

        assertThat(resource.getReadMembers()).isFalse();
    }

    @Test
    public void testReadMembersReturnFields() throws Exception {
        // when requesting 'members' as part of the return fields, should be reading the members
        ResourceState configState = this.client.read(new RequestContext.Builder().returnFields( new DefaultReturnFields("members")).build(), PATH + RESOURCE_ID);

        assertThat(configState).isNotNull();
        assertThat(configState.id()).isEqualTo(RESOURCE_ID);

        assertThat(resource.getReadMembers()).isTrue();
    }

    @Test
    public void testReadMembersAndOtherReturnFields() throws Exception {
        // when requesting 'members' as part of the return fields, should be reading the members
        ResourceState configState = this.client.read(new RequestContext.Builder().returnFields( new DefaultReturnFields("foo,members")).build(), PATH + RESOURCE_ID);

        assertThat(configState).isNotNull();
        assertThat(configState.id()).isEqualTo(RESOURCE_ID);

        assertThat(resource.getReadMembers()).isTrue();
    }

    @Test
    public void testReadMultiReturnFields() throws Exception {
        // when requesting 'members' as part of the return fields, should be reading the members
        ResourceState configState = this.client.read(new RequestContext.Builder().returnFields( new DefaultReturnFields("bar,foo, members")).build(), PATH + RESOURCE_ID);

        assertThat(configState).isNotNull();
        assertThat(configState.id()).isEqualTo(RESOURCE_ID);

        assertThat(resource.getReadMembers()).isFalse();
    }

    private class TestReadingMembersExtension implements Extension {

        @Override
        public void extend( SystemExtensionContext context ) throws Exception {
            // do nothing
        }

        @Override
        public void extend( ApplicationExtensionContext context ) throws Exception {
            resource = new TestReadingMembersResource( context.resourceId() );
            context.mountPublic(resource);
        }

        @Override
        public void unextend( ApplicationExtensionContext context ) throws Exception {
            //do nothing
        }
    }

    private class TestReadingMembersResource implements RootResource {

        Resource parent;
        String id;
        boolean readMembers = false;

        public TestReadingMembersResource(String id) {
            this.id = id;
        }

        public boolean getReadMembers() {
            return this.readMembers;
        }

        @Override
        public void parent( Resource parent ) {
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
        public void readMembers( RequestContext ctx, ResourceSink sink ) throws Exception {
            readMembers = true;
            sink.close();
        }
    }

}
