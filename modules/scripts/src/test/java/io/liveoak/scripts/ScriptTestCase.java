package io.liveoak.scripts;

import io.liveoak.spi.RequestContext;
import io.liveoak.spi.state.ResourceState;
import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;

/**
 * Tests the main scripting endpoint
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 */
public class ScriptTestCase extends BaseScriptTestCase {

    @Test
    public void testScriptingEndpoint() throws Exception {
        ResourceState result = client.read( new RequestContext.Builder().build(), SCRIPT_PATH);

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo("scripts");

        assertThat(getMember(result, "resource-triggered-scripts")).isNotNull();
        assertThat(getMember(result, "scheduled-scripts")).isNotNull();
    }

    //TODO: write up more tests
    // - check for the configuration properties: eg the directory name
    // - check that the configuration properties are not allowed to be updated
    // - etc...

}
