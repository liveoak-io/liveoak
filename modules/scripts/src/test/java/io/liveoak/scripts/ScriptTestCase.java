package io.liveoak.scripts;

import io.liveoak.common.codec.DefaultResourceState;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.UpdateNotSupportedException;
import io.liveoak.spi.state.ResourceState;
import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;

/**
 * Tests the main scripting endpoint
 *
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 * @author Ken Finnigan
 */
public class ScriptTestCase extends BaseScriptTestCase {

    @Test
    public void testScriptingEndpoint() throws Exception {
        ResourceState result = client.read(new RequestContext.Builder().build(), SCRIPT_PATH);

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo("scripts");

        assertThat(getMember(result, "resource-triggered-scripts")).isNotNull();
        assertThat(getMember(result, "scheduled-scripts")).isNotNull();
    }

    @Test(expected = UpdateNotSupportedException.class)
    public void testUnableToUpdateScriptResourceDirectory() throws Exception {
        client.update(new RequestContext.Builder().build(), SCRIPT_PATH, new DefaultResourceState());
    }

    @Test
    public void testReadScriptResourceDirectory() throws Exception {
        ResourceState result = client.read(new RequestContext.Builder().build(), SCRIPT_PATH);
        assertThat(result).isNotNull();
        assertThat(result.getProperty("script-directory")).isNotNull();
        assertThat(result.getProperty("script-directory").toString()).isEqualTo(scriptDirectory.getAbsolutePath());
    }

    //TODO: write up more tests

}
