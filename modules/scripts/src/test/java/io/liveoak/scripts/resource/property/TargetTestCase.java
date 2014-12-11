package io.liveoak.scripts.resource.property;

import io.liveoak.common.codec.DefaultResourceState;
import io.liveoak.scripts.JavaScriptResourceState;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.exceptions.ResourceNotFoundException;
import io.liveoak.spi.state.ResourceState;
import org.junit.Before;
import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;
import static org.fest.assertions.Fail.fail;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 */
public class TargetTestCase extends BasePropertyTestCase {
    @Before
    public void setupTests() throws Exception {
        //check that there are no other scripts configured
        ResourceState initialState = client.read(new RequestContext.Builder().build(), RESOURCE_SCRIPT_PATH);
        assertThat(initialState.members()).isEmpty();

        //create the metadata for the script
        ResourceState resourceState = client.create(new RequestContext.Builder().build(), RESOURCE_SCRIPT_PATH, new MetadataState("targetTest", "/testApp/*").libraries("client").build());
        client.create(new RequestContext.Builder().build(), "/testApp/mock/", new DefaultResourceState("foo"));
        client.create(new RequestContext.Builder().build(), "/testApp/mock/foo", new DefaultResourceState("bar"));
        assertThat(resourceState).isNotNull();
        assertThat(resourceState.id()).isEqualTo("targetTest");

        //upload the javascript file
        ResourceState binaryResourceState = new JavaScriptResourceState(readFile(TargetTestCase.class, "testExecuted.js"));
        ResourceState javascriptState = client.create(new RequestContext.Builder().build(), resourceState.uri().toString(), binaryResourceState);
        assertThat(javascriptState).isNotNull();
    }

    @Test
    public void testTarget() throws Exception {
        client.update(new RequestContext.Builder().build(), RESOURCE_SCRIPT_PATH + "/targetTest", new MetadataState("targetTest", "/testApp").libraries("client").build());
        check("/testApp", true);
        check("/testApp/mock", false);
        check("/testApp/mock/foo", false);
        check("/testApp/mock/foo/bar", false);

        client.update(new RequestContext.Builder().build(), RESOURCE_SCRIPT_PATH + "/targetTest", new MetadataState("targetTest", "/testApp*").libraries("client").build());
        check("/testApp", true);
        check("/testApp/mock", true);
        check("/testApp/mock/foo", false);
        check("/testApp/mock/foo/bar", false);

        client.update(new RequestContext.Builder().build(), RESOURCE_SCRIPT_PATH + "/targetTest", new MetadataState("targetTest", "/testApp**").libraries("client").build());
        check("/testApp", true);
        check("/testApp/mock", true);
        check("/testApp/mock/foo", true);
        check("/testApp/mock/foo/bar", true);

        client.update(new RequestContext.Builder().build(), RESOURCE_SCRIPT_PATH + "/targetTest", new MetadataState("targetTest", "/testApp/*").libraries("client").build());
        check("/testApp", false);
        check("/testApp/mock", true);
        check("/testApp/mock/foo", false);
        check("/testApp/mock/foo/bar", false);

        client.update(new RequestContext.Builder().build(), RESOURCE_SCRIPT_PATH + "/targetTest", new MetadataState("targetTest", "/testApp/**").libraries("client").build());
        check("/testApp", false);
        check("/testApp/mock", true);
        check("/testApp/mock/foo", true);
        check("/testApp/mock/foo/bar", true);

        //
        client.update(new RequestContext.Builder().build(), RESOURCE_SCRIPT_PATH + "/targetTest", new MetadataState("targetTest", "/testApp/mock").libraries("client").build());
        check("/testApp", false);
        check("/testApp/mock", true);
        check("/testApp/mock/foo", false);
        check("/testApp/mock/foo/bar", false);

        client.update(new RequestContext.Builder().build(), RESOURCE_SCRIPT_PATH + "/targetTest", new MetadataState("targetTest", "/testApp/mock*").libraries("client").build());
        check("/testApp", false);
        check("/testApp/mock", true);
        check("/testApp/mock/foo", true);
        check("/testApp/mock/foo/bar", false);

        client.update(new RequestContext.Builder().build(), RESOURCE_SCRIPT_PATH + "/targetTest", new MetadataState("targetTest", "/testApp/mock**").libraries("client").build());
        check("/testApp", false);
        check("/testApp/mock", true);
        check("/testApp/mock/foo", true);
        check("/testApp/mock/foo/bar", true);

        client.update(new RequestContext.Builder().build(), RESOURCE_SCRIPT_PATH + "/targetTest", new MetadataState("targetTest", "/testApp/mock/*").libraries("client").build());
        check("/testApp", false);
        check("/testApp/mock", false);
        check("/testApp/mock/foo", true);
        check("/testApp/mock/foo/bar", false);

        client.update(new RequestContext.Builder().build(), RESOURCE_SCRIPT_PATH + "/targetTest", new MetadataState("targetTest", "/testApp/mock/**").libraries("client").build());
        check("/testApp", false);
        check("/testApp/mock", false);
        check("/testApp/mock/foo", true);
        check("/testApp/mock/foo/bar", true);
    }

    protected void check(String path, boolean expected) throws Exception {
        execGet(path);
        checkIfExecuted(expected);
    }

    protected void checkIfExecuted(Boolean expected) throws Exception {
        if (expected) {
            ResourceState state = client.read(new RequestContext.Builder().build(), "/testApp/mock/executed");
            assertThat(state).isNotNull();

            assertThat(client.delete(new RequestContext.Builder().build(), "/testApp/mock/executed")).isNotNull();
        }

        try {
            client.read(new RequestContext.Builder().build(), "/testApp/mock/executed");
            fail();
        } catch (ResourceNotFoundException e) {
            //expected
        }
    }

}
