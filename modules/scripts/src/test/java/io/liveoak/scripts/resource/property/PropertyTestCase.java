package io.liveoak.scripts.resource.property;

import com.fasterxml.jackson.databind.JsonNode;
import io.liveoak.scripts.JavaScriptResourceState;
import io.liveoak.scripts.resource.BaseResourceTriggeredTestCase;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.state.ResourceState;
import org.junit.Before;
import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 */
public class PropertyTestCase extends BasePropertyTestCase {
    // TODO: test here a bunch of things such as enable/disable
    // priority
    // etc

    // NOTE: not meant to check things like CRUD on the properties but to test the properties behaviour.
    // Please see the io.liveoak.scripts.resource for CRUD testing

    @Before
    public void setupTests() throws Exception {
        //check that there are no other scripts configured
        ResourceState initialState = client.read(new RequestContext.Builder().build(), RESOURCE_SCRIPT_PATH);
        assertThat(initialState.members()).isEmpty();

        //create the metadata for the script
        ResourceState resourceState = client.create(new RequestContext.Builder().build(), RESOURCE_SCRIPT_PATH, new BaseResourceTriggeredTestCase.MetadataState("propertyTest", "/testApp/*").libraries("client").build());
        assertThat(resourceState).isNotNull();
        assertThat(resourceState.id()).isEqualTo("propertyTest");

        //upload the javascript file
        ResourceState binaryResourceState = new JavaScriptResourceState(readFile("testMetadata.js"));
        ResourceState javascriptState = client.create(new RequestContext.Builder().build(), resourceState.uri().toString(), binaryResourceState);
        assertThat(javascriptState).isNotNull();
    }


    @Test(timeout = 10000)
    public void testTimeout() throws Exception {
        Long startTime = System.currentTimeMillis();
        // Trigger a read
        JsonNode result = getJSON("/testApp/mock?test=testTimeout");
        Long endTime = System.currentTimeMillis();

        System.err.print("RESULT : " + result);
        System.err.println("EXECUTION TOOK : " + (endTime - startTime));

        Long executionTime = endTime - startTime;
        assertThat(executionTime).isGreaterThan(5000); //5000 is the timeout value
    }
}
