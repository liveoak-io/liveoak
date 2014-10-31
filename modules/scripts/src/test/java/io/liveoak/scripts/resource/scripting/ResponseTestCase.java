package io.liveoak.scripts.resource.scripting;

import java.util.Map;

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
        ResourceState binaryResourceState = new JavaScriptResourceState(readFile(ResponseTestCase.class, "testResponse.js"));
        ResourceState javascriptState = client.create(new RequestContext.Builder().build(), resourceState.uri().toString(), binaryResourceState);
        assertThat(javascriptState).isNotNull();
    }

    @Test
    public void responseTests() throws Exception {
        // Test #1 - Read
        // Trigger a read
        execGet("/testApp/mock/foo");

        ResourceState readState = client.read(new RequestContext.Builder().build(), "/testApp/mock/postRead");

        assertThat(readState.getProperty("type")).isEqualTo("read");
        testResource(readState);


        // Test #2 - Create
        // Trigger a create
        execPost("/testApp/mock", "{'id': 'ABC', 'foo' : 'bar'}");

        ResourceState postCreate = client.read(new RequestContext.Builder().build(), "/testApp/mock/postCreate");

        assertThat(postCreate.getProperty("type")).isEqualTo("created");
        assertThat(postCreate.getProperty("resource.id")).isEqualTo("ABC");
        assertThat(postCreate.getProperty("resource.uri")).isEqualTo("/testApp/mock/ABC");

        Map resourceProperties = (Map) postCreate.getProperty("resource.properties");
        assertThat(resourceProperties.get("foo")).isEqualTo("bar");


        // Test #3 - Update
        // Trigger an update
        execPut("/testApp/mock/foo", "{'Hello' : 'World'}");

        ResourceState updateState = client.read(new RequestContext.Builder().build(), "/testApp/mock/postUpdate");

        assertThat(updateState.getProperty("type")).isEqualTo("updated");
        assertThat(updateState.getProperty("resource.id")).isEqualTo("foo");
        assertThat(updateState.getProperty("resource.uri")).isEqualTo("/testApp/mock/foo");

        resourceProperties = (Map) updateState.getProperty("resource.properties");
        assertThat(resourceProperties.get("Hello")).isEqualTo("World");
        assertThat(resourceProperties.get("baz")).isNull();
        assertThat(resourceProperties.get("hello")).isNull();

        assertThat(updateState.getProperty("resource.member.0.id")).isEqualTo("baz");

        // Reset state
        execPut("/testApp/mock/foo", "{'hello' : 'world', 'baz' : 123 }");


        // Test #4 - Delete
        // Trigger a delete
        execDelete("/testApp/mock/foo");

        ResourceState deleteState = client.read(new RequestContext.Builder().build(), "/testApp/mock/postDelete");

        assertThat(deleteState.getProperty("type")).isEqualTo("deleted");
        testResource(deleteState);

        // Reset state
        ResourceState foo = new DefaultResourceState("foo");
        foo.putProperty("hello", "world");
        foo.putProperty("baz", 123);
        client.create(new RequestContext.Builder().build(), "/testApp/mock/", foo);


        // Test #5 - Error
        // Trigger an exception to be thrown in the resource
        assertThat(execPut("/testApp/mock", "{'throwError': 'true'}")).hasStatus(406);

        ResourceState onErrorState = client.read(new RequestContext.Builder().build(), "/testApp/mock/onError");

        assertThat(onErrorState.getProperty("type")).isEqualTo("error");
        assertThat(onErrorState.getProperty("resource.id")).isNull();
        assertThat(onErrorState.getProperty("resource.uri")).isNull();

        resourceProperties = (Map) onErrorState.getProperty("resource.properties");
        assertThat(resourceProperties.get("error-type")).isEqualTo("NOT_ACCEPTABLE");
        assertThat(resourceProperties.get("cause")).isNotNull();
        assertThat(resourceProperties.get("message")).isNotNull();


        // Test #6 - Set type
        // Trigger a read
        assertThat(execGet("/testApp/mock/foo?test=setType")).hasStatus(406);
        assertThat(httpResponse).isNotAcceptable().with("type cannot be modified");


        // Test #7 - Set resource
        // Trigger a read
        assertThat(execGet("/testApp/mock/foo?test=setResource")).hasStatus(406);
        assertThat(httpResponse).isNotAcceptable().with("resource cannot be modified");


        // Test #8 - Set request
        // Trigger a read
        assertThat(execGet("/testApp/mock/foo?test=setRequest")).hasStatus(406);
        assertThat(httpResponse).isNotAcceptable().with("request cannot be modified");
    }

    protected void testResource(ResourceState resourceState) throws Exception {
        assertThat(resourceState.getProperty("resource.id")).isEqualTo("foo");
        assertThat(resourceState.getProperty("resource.uri")).isEqualTo("/testApp/mock/foo");

        Map resourceProperties = (Map) resourceState.getProperty("resource.properties");
        assertThat(resourceProperties.get("hello")).isEqualTo("world");
        assertThat(resourceProperties.get("baz")).isEqualTo(123);

        assertThat(resourceState.getProperty("resource.member.0.id")).isEqualTo("baz");
    }
}
