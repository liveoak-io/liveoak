package io.liveoak.scripts.resource.scripting;

import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import io.liveoak.common.codec.DefaultResourceState;
import io.liveoak.scripts.JavaScriptResourceState;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.state.ResourceState;
import org.junit.Before;
import org.junit.Test;

import static io.liveoak.testtools.assertions.Assertions.assertThat;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 */
public class ResponseTestCase extends BaseScriptingTestCase {

    @Before
    public void setupTests() throws Exception {
        //check that there are no other scripts configured
        ResourceState initialState = client.read(new RequestContext.Builder().build(), RESOURCE_SCRIPT_PATH);
        assertThat(initialState.members()).isEmpty();

        //create the metadata for the script
        ResourceState resourceState = client.create(new RequestContext.Builder().build(), RESOURCE_SCRIPT_PATH, new MetadataState("requestTest", "/testApp/**").libraries("client").build());

        ResourceState foo = new DefaultResourceState("foo");
        foo.putProperty("hello", "world");
        foo.putProperty("baz", 123);

        client.create(new RequestContext.Builder().build(), "/testApp/mock/", foo);

        ResourceState baz = new DefaultResourceState("baz");
        client.create(new RequestContext.Builder().build(), "/testApp/mock/foo", baz);

        assertThat(resourceState).isNotNull();
        assertThat(resourceState.id()).isEqualTo("requestTest");

        //upload the javascript file
        ResourceState binaryResourceState = new JavaScriptResourceState(readFile("testResponse.js"));
        ResourceState javascriptState = client.create(new RequestContext.Builder().build(), resourceState.uri().toString(), binaryResourceState);
        assertThat(javascriptState).isNotNull();
    }

    @Test
    public void testRead() throws Exception {
        // Trigger a read
        execGet("/testApp/mock/foo");

        ResourceState readState = client.read(new RequestContext.Builder().build(), "/testApp/mock/postRead");

        assertThat(readState.getProperty("type")).isEqualTo("read");
        testResource(readState);
    }

    @Test
    public void testCreate() throws Exception {
        // Trigger a create
        execPost("/testApp/mock", "{'id': 'ABC', 'foo' : 'bar'}");

        ResourceState postCreate = client.read(new RequestContext.Builder().build(), "/testApp/mock/postCreate");

        assertThat(postCreate.getProperty("type")).isEqualTo("created");
        assertThat(postCreate.getProperty("resource.id")).isEqualTo("ABC");
        assertThat(postCreate.getProperty("resource.uri")).isEqualTo("/testApp/mock/ABC");

        Map resourceProperties = (Map) postCreate.getProperty("resource.properties");
        assertThat(resourceProperties.get("foo")).isEqualTo("bar");
    }

    @Test
    public void testUpdate() throws Exception {
        // Trigger an update
        execPut("/testApp/mock/foo", "{'Hello' : 'World'}");

        ResourceState updateState = client.read(new RequestContext.Builder().build(), "/testApp/mock/postUpdate");

        assertThat(updateState.getProperty("type")).isEqualTo("updated");
        assertThat(updateState.getProperty("resource.id")).isEqualTo("foo");
        assertThat(updateState.getProperty("resource.uri")).isEqualTo("/testApp/mock/foo");

        Map resourceProperties = (Map) updateState.getProperty("resource.properties");
        assertThat(resourceProperties.get("Hello")).isEqualTo("World");
        assertThat(resourceProperties.get("baz")).isNull();
        assertThat(resourceProperties.get("hello")).isNull();

        assertThat(updateState.getProperty("resource.member.0.id")).isEqualTo("baz");
    }

    @Test
    public void testDelete() throws Exception {
        // Trigger a delete
        execDelete("/testApp/mock/foo");

        ResourceState deleteState = client.read(new RequestContext.Builder().build(), "/testApp/mock/postDelete");

        assertThat(deleteState.getProperty("type")).isEqualTo("deleted");
        testResource(deleteState);
    }

    @Test
    public void testError() throws Exception {
        // Trigger an exception to be thrown in the resource
        assertThat(execPut("/testApp/mock", "{'throwError': 'true'}")).hasStatus(406);

        ResourceState onErrorState = client.read(new RequestContext.Builder().build(), "/testApp/mock/onError");

        assertThat(onErrorState.getProperty("type")).isEqualTo("error");
        assertThat(onErrorState.getProperty("resource.id")).isNull();
        assertThat(onErrorState.getProperty("resource.uri")).isNull();

        Map resourceProperties = (Map) onErrorState.getProperty("resource.properties");
        assertThat(resourceProperties.get("error-type")).isEqualTo("NOT_ACCEPTABLE");
        assertThat(resourceProperties.get("cause")).isNotNull();
        assertThat(resourceProperties.get("message")).isNotNull();
    }

    protected void testResource(ResourceState resourceState) throws Exception {
        assertThat(resourceState.getProperty("resource.id")).isEqualTo("foo");
        assertThat(resourceState.getProperty("resource.uri")).isEqualTo("/testApp/mock/foo");

        Map resourceProperties = (Map) resourceState.getProperty("resource.properties");
        assertThat(resourceProperties.get("hello")).isEqualTo("world");
        assertThat(resourceProperties.get("baz")).isEqualTo(123);

        assertThat(resourceState.getProperty("resource.member.0.id")).isEqualTo("baz");
    }

    @Test
    public void testSetType() throws Exception {
        // Trigger a read
        assertThat(execGet("/testApp/mock/foo?test=setType")).hasStatus(406);
        JsonNode result = toJSON(httpResponse.getEntity());

        assertThat(result.get("error-type").textValue()).isEqualTo("NOT_ACCEPTABLE");
        assertThat(result.get("message").textValue()).isEqualTo("type cannot be modified");
    }

    @Test
    public void testSetResource() throws Exception {
        // Trigger a read
        assertThat(execGet("/testApp/mock/foo?test=setResource")).hasStatus(406);
        JsonNode result = toJSON(httpResponse.getEntity());

        assertThat(result.get("error-type").textValue()).isEqualTo("NOT_ACCEPTABLE");
        assertThat(result.get("message").textValue()).isEqualTo("resource cannot be modified");
    }

    @Test
    public void testSetRequest() throws Exception {
        // Trigger a read
        assertThat(execGet("/testApp/mock/foo?test=setRequest")).hasStatus(406);
        JsonNode result = toJSON(httpResponse.getEntity());

        assertThat(result.get("error-type").textValue()).isEqualTo("NOT_ACCEPTABLE");
        assertThat(result.get("message").textValue()).isEqualTo("request cannot be modified");
    }
}
