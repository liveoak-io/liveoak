package io.liveoak.scripts.resource.scripting;

import java.util.ArrayList;
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
    public void requestContextTests() throws Exception {
        // Test #1 - Read
        // Trigger a read
        String uri = "/testApp/mock/foo?offset=1&limit=5&fields=*(*)&foo=bar";
        execGet(uri);

        ResourceState readState = client.read(new RequestContext.Builder().build(), "/testApp/mock/preRead");
        testState(readState, uri, "GET");


        // Test #2 - Create
        // Trigger a create
        uri = "/testApp/mock/foo?offset=1&limit=5&fields=*(*)&foo=bar";
        execPost(uri, "{'id': 'ABC', 'foo' : 'bar'}");

        ResourceState createState = client.read(new RequestContext.Builder().build(), "/testApp/mock/preCreate");
        testState(createState, uri, "POST");


        // Test #3 - Update
        // Trigger an update
        uri = "/testApp/mock/foo?offset=1&limit=5&fields=*(*)&foo=bar";
        execPut(uri, "{'id': 'ABC', 'foo' : 'bar'}");

        ResourceState updateState = client.read(new RequestContext.Builder().build(), "/testApp/mock/preUpdate");
        testState(updateState, uri, "PUT");


        // Test #4 - Delete
        // Trigger a delete
        uri = "/testApp/mock/foo?offset=1&limit=5&fields=*(*)&foo=bar";
        execDelete(uri);

        ResourceState deleteState = client.read(new RequestContext.Builder().build(), "/testApp/mock/preDelete");
        testState(deleteState, uri, "DELETE");


        // Test #5 - Set parameters
        // Trigger a read
        assertThat(get("/testApp/mock/foo?test=setParameters").execute()).hasStatus(406);
        assertThat(httpResponse).isNotAcceptable().with("parameters cannot be modified");


        // Test #6 - Set attributes
        // Trigger a read
        assertThat(get("/testApp/mock/foo?test=setAttributes").execute()).hasStatus(406);
        assertThat(httpResponse).isNotAcceptable().with("attributes cannot be modified");


        // Test #7 - Set security context
        // Trigger a read
        assertThat(get("/testApp/mock/foo?test=setSecurityContext").execute()).hasStatus(406);
        assertThat(httpResponse).isNotAcceptable().with("securityContext cannot be modified");
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
