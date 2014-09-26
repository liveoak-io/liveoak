package io.liveoak.scripts.resource.scripting;

import com.fasterxml.jackson.databind.JsonNode;
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
    public void testSetId() throws Exception {
        // Trigger a read
        assertThat(get("/testApp/mock/foo?test=setId").execute()).hasStatus(406);
        JsonNode result = toJSON(httpResponse.getEntity());

        assertThat(result.get("error-type").textValue()).isEqualTo("NOT_ACCEPTABLE");
        assertThat(result.get("message").textValue()).isEqualTo("id cannot be modified");
    }

    @Test
    public void testSetPath() throws Exception {
        // Trigger a read
        assertThat(get("/testApp/mock/foo?test=setPath").execute()).hasStatus(406);
        JsonNode result = toJSON(httpResponse.getEntity());

        assertThat(result.get("error-type").textValue()).isEqualTo("NOT_ACCEPTABLE");
        assertThat(result.get("message").textValue()).isEqualTo("path cannot be modified");
    }

    @Test
    public void testSetType() throws Exception {
        // Trigger a read
        assertThat(get("/testApp/mock/foo?test=setType").execute()).hasStatus(406);
        JsonNode result = toJSON(httpResponse.getEntity());

        assertThat(result.get("error-type").textValue()).isEqualTo("NOT_ACCEPTABLE");
        assertThat(result.get("message").textValue()).isEqualTo("type cannot be modified");
    }

    @Test
    public void testSetResource() throws Exception {
        // Trigger a read
        assertThat(get("/testApp/mock/foo?test=setResource").execute()).hasStatus(406);
        JsonNode result = toJSON(httpResponse.getEntity());

        assertThat(result.get("error-type").textValue()).isEqualTo("NOT_ACCEPTABLE");
        assertThat(result.get("message").textValue()).isEqualTo("resource cannot be modified");
    }
}
