package io.liveoak.scripts;

import io.liveoak.common.codec.DefaultResourceState;
import io.liveoak.scripts.resource.ScriptConfig;
import io.liveoak.spi.NotAcceptableException;
import io.liveoak.spi.RequestContext;
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

    @Test(expected = NotAcceptableException.class)
    public void testEmptyConfigUpdate() throws Exception {
        client.update(new RequestContext.Builder().build(), SCRIPT_PATH, new DefaultResourceState());
    }

    @Test
    public void testUpdateConfig() throws Exception {
        ResourceState config = new DefaultResourceState();
        config.putProperty("script-directory", scriptDirectory.getAbsolutePath());
        client.update(new RequestContext.Builder().build(), SCRIPT_PATH, config);

        try {
            config.putProperty("script-directory", scriptDirectory.getParentFile().getAbsolutePath());
            client.update(new RequestContext.Builder().build(), SCRIPT_PATH, config);
        } catch (NotAcceptableException e) {
            //expected
        }
        // read the result to make sure nothing was changed
        testReadScriptConfig();

        // try and modify the timeout
        config.putProperty("script-directory", scriptDirectory.getAbsolutePath());
        config.putProperty("default-timeout", 10000);
        ResourceState result = client.update(new RequestContext.Builder().build(), SCRIPT_PATH, config);
        assertThat(result.getProperty("script-directory").toString()).isEqualTo(scriptDirectory.getAbsolutePath());
        assertThat((Integer)result.getProperty("default-timeout")).isEqualTo(10000);

        // try and modify the timout with an invalid entry
        config.putProperty("default-timeout", "foo");
        try {
            client.update(new RequestContext.Builder().build(), SCRIPT_PATH, config);
        } catch (NotAcceptableException e) {
            // expected
        }

        config.putProperty("default-timeout", -10);
        try {
            client.update(new RequestContext.Builder().build(), SCRIPT_PATH, config);
        } catch (NotAcceptableException e) {
            // expected
        }
    }

    @Test
    public void testReadScriptConfig() throws Exception {
        ResourceState result = client.read(new RequestContext.Builder().build(), SCRIPT_PATH);
        assertThat(result).isNotNull();
        assertThat(result.getProperty("script-directory")).isNotNull();
        assertThat(result.getProperty("script-directory").toString()).isEqualTo(scriptDirectory.getAbsolutePath());
        assertThat(result.getProperty("default-timeout")).isNotNull();
        assertThat((Integer)result.getProperty("default-timeout")).isEqualTo(ScriptConfig.DEFAULT_TIMEOUT);
    }
}
