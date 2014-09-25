package io.liveoak.scripts.resource.scripting;

import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import io.liveoak.common.codec.DefaultResourceState;
import io.liveoak.common.util.ObjectMapperFactory;
import io.liveoak.scripts.JavaScriptResourceState;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.state.ResourceState;
import org.junit.Before;
import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;

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
        ResourceState resourceState = client.create(new RequestContext.Builder().build(), RESOURCE_SCRIPT_PATH, new MetadataState("requestTest", "/testApp/mock/*").libraries("client").build());

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
        httpGet("http://localhost:8080/testApp/mock/foo");

        ResourceState readState = client.read(new RequestContext.Builder().build(), "/testApp/mock/postRead");

        assertThat(readState.getProperty("type")).isEqualTo("read");
        testResource(readState);
    }

    @Test
    public void testCreate() throws Exception {
        // Trigger a create
        JsonNode postObject = ObjectMapperFactory.create().readTree("{'id': 'ABC', 'foo' : 'bar'}");
        createResource("http://localhost:8080/testApp/mock", postObject);

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
        JsonNode putObject = ObjectMapperFactory.create().readTree("{'Hello' : 'World'}");
        updateResource("http://localhost:8080/testApp/mock/foo", putObject);

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
        httpDelete("http://localhost:8080/testApp/mock/foo");

        ResourceState deleteState = client.read(new RequestContext.Builder().build(), "/testApp/mock/postDelete");

        assertThat(deleteState.getProperty("type")).isEqualTo("deleted");
        testResource(deleteState);
    }

    @Test
    public void testError() throws Exception {

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
