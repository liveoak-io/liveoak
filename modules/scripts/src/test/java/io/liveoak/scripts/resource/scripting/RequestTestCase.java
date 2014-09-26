package io.liveoak.scripts.resource.scripting;

import io.liveoak.common.codec.DefaultResourceState;
import io.liveoak.scripts.JavaScriptResourceState;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.state.ResourceState;
import org.junit.Before;
import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 */
public class RequestTestCase extends BaseScriptingTestCase {

    @Before
    public void setupTests() throws Exception {
        //check that there are no other scripts configured
        ResourceState initialState = client.read(new RequestContext.Builder().build(), RESOURCE_SCRIPT_PATH);
        assertThat(initialState.members()).isEmpty();

        //create the metadata for the script
        ResourceState resourceState = client.create(new RequestContext.Builder().build(), RESOURCE_SCRIPT_PATH, new MetadataState("requestTest", "/testApp/mock/*").libraries("client").build());
        client.create(new RequestContext.Builder().build(), "/testApp/mock/", new DefaultResourceState("foo"));
        assertThat(resourceState).isNotNull();
        assertThat(resourceState.id()).isEqualTo("requestTest");

        //upload the javascript file
        ResourceState binaryResourceState = new JavaScriptResourceState(readFile("testRequest.js"));
        ResourceState javascriptState = client.create(new RequestContext.Builder().build(), resourceState.uri().toString(), binaryResourceState);
        assertThat(javascriptState).isNotNull();
    }

    @Test
    public void testCreate() throws Exception {
        // Trigger a create
        execPost("/testApp/mock/", "{'foo' : 'BAR'}");

        ResourceState preCreate = client.read(new RequestContext.Builder().build(), "/testApp/mock/preCreate");
        ResourceState postCreate = client.read(new RequestContext.Builder().build(), "/testApp/mock/postCreate");

        assertThat(preCreate.getProperty("id")).isEqualTo(postCreate.getProperty("id"));
        assertThat(preCreate.getProperty("type")).isEqualTo(postCreate.getProperty("type")).isEqualTo("create");
        assertThat(preCreate.getProperty("path")).isEqualTo(postCreate.getProperty("path")).isEqualTo("/testApp/mock");
    }

    @Test
    public void testRead() throws Exception {
        // Trigger a read
        execGet("/testApp/mock/foo");

        ResourceState preRead = client.read(new RequestContext.Builder().build(), "/testApp/mock/preRead");
        ResourceState postRead = client.read(new RequestContext.Builder().build(), "/testApp/mock/postRead");

        assertThat(preRead.getProperty("id")).isEqualTo(postRead.getProperty("id"));
        assertThat(preRead.getProperty("type")).isEqualTo(postRead.getProperty("type")).isEqualTo("read");
        assertThat(preRead.getProperty("path")).isEqualTo(postRead.getProperty("path")).isEqualTo("/testApp/mock/foo");
    }

    @Test
    public void testUpdate() throws Exception {
        // Trigger an update
        execPut("/testApp/mock/foo", "{'foo' : 'BAR'}");

        ResourceState preUpdate = client.read(new RequestContext.Builder().build(), "/testApp/mock/preUpdate");
        ResourceState postUpdate = client.read(new RequestContext.Builder().build(), "/testApp/mock/postUpdate");

        assertThat(preUpdate.getProperty("id")).isEqualTo(postUpdate.getProperty("id"));
        assertThat(preUpdate.getProperty("type")).isEqualTo(postUpdate.getProperty("type")).isEqualTo("update");
        assertThat(preUpdate.getProperty("path")).isEqualTo(postUpdate.getProperty("path")).isEqualTo("/testApp/mock/foo");
    }

    @Test
    public void testDelete() throws Exception {
        // Trigger an update
        execDelete("/testApp/mock/foo");

        ResourceState preDelete = client.read(new RequestContext.Builder().build(), "/testApp/mock/preDelete");
        ResourceState postDelete = client.read(new RequestContext.Builder().build(), "/testApp/mock/postDelete");

        assertThat(preDelete.getProperty("id")).isEqualTo(postDelete.getProperty("id"));
        assertThat(preDelete.getProperty("type")).isEqualTo(postDelete.getProperty("type")).isEqualTo("delete");
        assertThat(preDelete.getProperty("path")).isEqualTo(postDelete.getProperty("path")).isEqualTo("/testApp/mock/foo");
    }

}
