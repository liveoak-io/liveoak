package io.liveoak.scripts.resource.scripting.resource;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import io.liveoak.common.codec.DefaultResourceState;
import io.liveoak.scripts.JavaScriptResourceState;
import io.liveoak.scripts.resource.BaseResourceTriggeredTestCase;
import io.liveoak.scripts.resource.scripting.BaseScriptingTestCase;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.state.ResourceState;
import org.junit.Before;
import org.junit.Test;

import static io.liveoak.testtools.assertions.Assertions.assertThat;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 */
public class ResourceTestCase extends BaseScriptingTestCase {

    @Before
    public void setupTests() throws Exception {
        //check that there are no other scripts configured
        ResourceState initialState = client.read(new RequestContext.Builder().build(), RESOURCE_SCRIPT_PATH);
        assertThat(initialState.members()).isEmpty();

        ResourceState foo = new DefaultResourceState("foo");
        foo.putProperty("hello", "world");
        foo.putProperty("ABC", 123);
        client.create(new RequestContext.Builder().build(), "/testApp/mock/", foo);

        ResourceState bar = new DefaultResourceState("bar");
        bar.putProperty("XYZ", "098");
        client.create(new RequestContext.Builder().build(), "/testApp/mock/foo", bar);

        //create the metadata for the script
        ResourceState resourceState = client.create(new RequestContext.Builder().build(), RESOURCE_SCRIPT_PATH, new BaseResourceTriggeredTestCase.MetadataState("requestTest", "/testApp/mock/*").libraries("client").build());
        assertThat(resourceState).isNotNull();
        assertThat(resourceState.id()).isEqualTo("requestTest");

        //upload the javascript file
        ResourceState binaryResourceState = new JavaScriptResourceState(readFile("testResource.js"));
        ResourceState javascriptState = client.create(new RequestContext.Builder().build(), resourceState.uri().toString(), binaryResourceState);
        assertThat(javascriptState).isNotNull();
    }

    @Test
    public void resourceTests() throws Exception {
        // Test #1 - Set id
        // Trigger a read
        JsonNode result = getJSON("/testApp/mock/foo?test=setId");
        assertThat(result.get("id").textValue()).isEqualTo("bar");
        assertThat(result.get("self").get("href").textValue()).isEqualTo("/testApp/mock/foo");
        assertThat(result.get("hello").textValue()).isEqualTo("world");
        assertThat(result.get("ABC").getNodeType()).isEqualTo(JsonNodeType.NUMBER);
        assertThat(result.get("ABC").asInt()).isEqualTo(123);


        // Test #2 - Set path
        // Trigger a read
        result = getJSON("/testApp/mock/foo?test=setPath");
        assertThat(result.get("id").textValue()).isEqualTo("foo");
        assertThat(result.get("self").get("href").textValue()).isEqualTo("foobar");
        assertThat(result.get("hello").textValue()).isEqualTo("world");
        assertThat(result.get("ABC").getNodeType()).isEqualTo(JsonNodeType.NUMBER);
        assertThat(result.get("ABC").asInt()).isEqualTo(123);


        // Test #3 - Set members
        // Trigger a read
        assertThat(execGet("/testApp/mock/foo?test=setMembers")).hasStatus(406);
        assertThat(httpResponse).isNotAcceptable().with("members cannot be modified");


        // Test #4 - Set properties
        // Trigger a read
        result = getJSON("/testApp/mock/foo?test=setProperties");
        assertThat(result.get("id").textValue()).isEqualTo("foo");
        assertThat(result.get("self").get("href").textValue()).isEqualTo("/testApp/mock/foo");
        assertThat(result.get("testing").textValue()).isEqualTo("123");
        assertThat(result.get("hello")).isNull();
        assertThat(result.get("ABC")).isNull();
    }
}
