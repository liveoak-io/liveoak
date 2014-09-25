package io.liveoak.scripts.resource.scripting;

import java.util.ArrayList;
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
public class RequestContextTestCase extends BaseScriptingTestCase {
    //TODO: add in tests for security context where the user is authenticated by the system.

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
        ResourceState binaryResourceState = new JavaScriptResourceState(readFile("testRequestContext.js"));
        ResourceState javascriptState = client.create(new RequestContext.Builder().build(), resourceState.uri().toString(), binaryResourceState);
        assertThat(javascriptState).isNotNull();
    }

    @Test
    public void testRead() throws Exception {
        // Trigger a read
        String uri = "/testApp/mock/foo?offset=1&limit=5&fields=*(*)&foo=bar";
        httpGet("http://localhost:8080" + uri);

        ResourceState readState = client.read(new RequestContext.Builder().build(), "/testApp/mock/preRead");
        testState(readState, uri, "GET");
    }

    @Test
    public void testCreate() throws Exception {
        // Trigger a create
        String uri = "/testApp/mock/foo?offset=1&limit=5&fields=*(*)&foo=bar";
        JsonNode postObject = ObjectMapperFactory.create().readTree("{'id': 'ABC', 'foo' : 'bar'}");
        createResource("http://localhost:8080" + uri, postObject);

        ResourceState createState = client.read(new RequestContext.Builder().build(), "/testApp/mock/preCreate");
        testState(createState, uri, "POST");
    }

    @Test
    public void testUpdate() throws Exception {
        // Trigger an update
        String uri = "/testApp/mock/foo?offset=1&limit=5&fields=*(*)&foo=bar";
        JsonNode putObject = ObjectMapperFactory.create().readTree("{'id': 'ABC', 'foo' : 'bar'}");
        updateResource("http://localhost:8080" + uri, putObject);

        ResourceState updateState = client.read(new RequestContext.Builder().build(), "/testApp/mock/preUpdate");
        testState(updateState, uri, "PUT");
    }

    @Test
    public void testDelete() throws Exception {
        // Trigger a delete
        String uri = "/testApp/mock/foo?offset=1&limit=5&fields=*(*)&foo=bar";
        httpDelete("http://localhost:8080" + uri);

        ResourceState deleteState = client.read(new RequestContext.Builder().build(), "/testApp/mock/preDelete");
        testState(deleteState, uri, "DELETE");
    }

    public void testState(ResourceState state, String uri, String httpMethod) {
        Map attributes = (Map) state.getProperty("attributes");
        Map parameters = (Map) state.getProperty("parameters");
        Map security = (Map) state.getProperty("securityContext");
        assertThat(parameters.get("offset")).isEqualTo("1");
        assertThat(parameters.get("limit")).isEqualTo("4"); //The script changes this value
        assertThat(parameters.get("fields")).isEqualTo("*(*)");
        assertThat(parameters.get("foo")).isEqualTo("bar");
        assertThat(parameters.get("baz")).isEqualTo("bat");

        assertThat(((Map) attributes.get("HTTP_REQUEST")).get("method")).isEqualTo(httpMethod);
        assertThat(((Map) attributes.get("HTTP_REQUEST")).get("uri")).isEqualTo(uri);

        assertThat(security.get("authenticated")).isEqualTo(false);
        assertThat(security.get("realms")).isEqualTo(null);
        assertThat(security.get("roles")).isEqualTo(new ArrayList());
        assertThat(security.get("subject")).isEqualTo(null);
        assertThat(security.get("lastVerified")).isEqualTo(0L);
        assertThat(security.get("token")).isEqualTo(null);
    }

}
