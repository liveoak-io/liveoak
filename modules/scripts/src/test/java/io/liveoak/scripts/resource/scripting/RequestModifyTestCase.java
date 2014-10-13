package io.liveoak.scripts.resource.scripting;

import io.liveoak.common.codec.DefaultResourceState;
import io.liveoak.scripts.JavaScriptResourceState;
import io.liveoak.scripts.resource.BaseResourceTriggeredTestCase;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.state.ResourceState;
import org.junit.Before;
import org.junit.Test;

import static io.liveoak.testtools.assertions.Assertions.assertThat;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 */
public class RequestModifyTestCase extends BaseScriptingTestCase {
    @Before
    public void setupTests() throws Exception {
        //check that there are no other scripts configured
        ResourceState initialState = client.read(new RequestContext.Builder().build(), RESOURCE_SCRIPT_PATH);
        assertThat(initialState.members()).isEmpty();
        client.create(new RequestContext.Builder().build(), "/testApp/mock/", new DefaultResourceState("foo"));

        //create the metadata for the script
        ResourceState resourceState = client.create(new RequestContext.Builder().build(), RESOURCE_SCRIPT_PATH, new BaseResourceTriggeredTestCase.MetadataState("requestTest", "/testApp/mock/*").libraries("client").build());
        assertThat(resourceState).isNotNull();
        assertThat(resourceState.id()).isEqualTo("requestTest");

        //upload the javascript file
        ResourceState binaryResourceState = new JavaScriptResourceState(readFile("testRequestModify.js"));
        ResourceState javascriptState = client.create(new RequestContext.Builder().build(), resourceState.uri().toString(), binaryResourceState);
        assertThat(javascriptState).isNotNull();
    }

    @Test
    public void requestModificationTests() throws Exception {
        // Test #1 - Set id
        // Trigger a read
        assertThat(execGet("/testApp/mock/foo?test=setId")).hasStatus(406);
        assertThat(httpResponse).isNotAcceptable().with("id cannot be modified");


        // Test #2 - Set path
        // Trigger a read
        assertThat(execGet("/testApp/mock/foo?test=setPath")).hasStatus(406);
        assertThat(httpResponse).isNotAcceptable().with("path cannot be modified");


        // Test #3 - Set type
        // Trigger a read
        assertThat(execGet("/testApp/mock/foo?test=setType")).hasStatus(406);
        assertThat(httpResponse).isNotAcceptable().with("type cannot be modified");


        // Test #4 - Set resource
        // Trigger a read
        assertThat(execGet("/testApp/mock/foo?test=setResource")).hasStatus(406);
        assertThat(httpResponse).isNotAcceptable().with("resource cannot be modified");
    }
}
