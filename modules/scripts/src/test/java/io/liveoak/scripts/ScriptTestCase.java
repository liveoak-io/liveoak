package io.liveoak.scripts;

import io.liveoak.common.codec.DefaultResourceState;
import io.liveoak.scripts.resource.ScriptConfig;
import io.liveoak.spi.exceptions.NotAcceptableException;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.state.ResourceState;
import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;
import static org.junit.Assert.fail;

/**
 * Tests the main scripting endpoint
 *
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 * @author Ken Finnigan
 */
public class ScriptTestCase extends BaseScriptTestCase {

    @Test
    public void scriptTests() throws Exception {
        // Test #1 - Scripting endpoint
        ResourceState result = client.read(new RequestContext.Builder().build(), SCRIPT_PATH);

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo("scripts");

        assertThat(getMember(result, "resource-triggered-scripts")).isNotNull();


        // Test #2 - Empty config update
        try {
            client.update(new RequestContext.Builder().build(), SCRIPT_PATH, new DefaultResourceState());
            fail("NotAcceptableException not thrown.");
        } catch (NotAcceptableException nae) {
            // expected
        }


        // Test #3 - Update config
        ResourceState config = new DefaultResourceState();
        config.putProperty("script-directory", scriptDirectory.getAbsolutePath());
        client.update(new RequestContext.Builder().build(), SCRIPT_PATH, config);

        try {
            config.putProperty("script-directory", scriptDirectory.getParentFile().getAbsolutePath());
            client.update(new RequestContext.Builder().build(), SCRIPT_PATH, config);
            fail("NotAcceptableException not thrown.");
        } catch (NotAcceptableException e) {
            //expected
        }

        // read the result to make sure nothing was changed
        readScriptConfig();

        // try and modify the timeout
        config.putProperty("script-directory", scriptDirectory.getAbsolutePath());
        config.putProperty("default-timeout", 10000);
        result = client.update(new RequestContext.Builder().build(), SCRIPT_PATH, config);
        assertThat(result.getProperty("script-directory").toString()).isEqualTo(scriptDirectory.getAbsolutePath());
        assertThat((Integer)result.getProperty("default-timeout")).isEqualTo(10000);

        // try and modify the timout with an invalid entry
        config.putProperty("default-timeout", "foo");
        try {
            client.update(new RequestContext.Builder().build(), SCRIPT_PATH, config);
            fail("NotAcceptableException not thrown.");
        } catch (NotAcceptableException e) {
            // expected
        }

        config.putProperty("default-timeout", -10);
        try {
            client.update(new RequestContext.Builder().build(), SCRIPT_PATH, config);
            fail("NotAcceptableException not thrown.");
        } catch (NotAcceptableException e) {
            // expected
        }

        // Reset timeout
        config.putProperty("script-directory", scriptDirectory.getAbsolutePath());
        config.putProperty("default-timeout", 5000);
        client.update(new RequestContext.Builder().build(), SCRIPT_PATH, config);


        // Test #4 - Read script config
        readScriptConfig();
    }

    public void readScriptConfig() throws Exception {
        ResourceState result = client.read(new RequestContext.Builder().build(), SCRIPT_PATH);
        assertThat(result).isNotNull();
        assertThat(result.getProperty("script-directory")).isNotNull();
        assertThat(result.getProperty("script-directory").toString()).isEqualTo(scriptDirectory.getAbsolutePath());
        assertThat(result.getProperty("default-timeout")).isNotNull();
        assertThat((Integer)result.getProperty("default-timeout")).isEqualTo(ScriptConfig.DEFAULT_TIMEOUT);
    }
}
