package io.liveoak.redirect.https;

import io.liveoak.common.codec.DefaultResourceState;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.exceptions.DeleteNotSupportedException;
import io.liveoak.spi.exceptions.ResourceException;
import io.liveoak.spi.state.ResourceState;
import org.junit.Before;
import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;
import static org.fest.assertions.Fail.fail;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 */
public class RedirectConfigTestCase extends BaseHttpsRedirectTestCase {

    private static final String SYSTEM_CONFIG_PATH = "/admin/system/https-redirect/module";

    @Before
    public void resetDefaultConfig() throws Exception {
        ResourceState config = new DefaultResourceState();

        ResourceState defaultConfig = new DefaultResourceState();
        defaultConfig.putProperty("redirects", "SECURED");
        defaultConfig.putProperty("redirect-type", "TEMPORARY");
        defaultConfig.putProperty("max-age", 300);

        config.putProperty("default", defaultConfig);

        ResourceState resourceState = client.update(new RequestContext.Builder().build(), SYSTEM_CONFIG_PATH, config);
    }

    @Test
    public void testReadConfig() throws Exception {
        ResourceState resourceState = client.read(new RequestContext.Builder().build(), SYSTEM_CONFIG_PATH);
        checkConfig(resourceState, "SECURED", "TEMPORARY", 300);
    }

    @Test
    public void testUpdateConfig() throws Exception {
        ResourceState updatedConfig = new DefaultResourceState();

        ResourceState defaultConfig = new DefaultResourceState();
        defaultConfig.putProperty("redirects", "ALL");
        defaultConfig.putProperty("redirect-type", "PERMANENT");
        defaultConfig.putProperty("max-age", 123);

        updatedConfig.putProperty("default", defaultConfig);

        ResourceState updatedState = client.update(new RequestContext.Builder().build(), SYSTEM_CONFIG_PATH, updatedConfig);
        checkConfig(updatedState, "ALL", "PERMANENT", 123);

        ResourceState readState = client.read(new RequestContext.Builder().build(), SYSTEM_CONFIG_PATH);
        checkConfig(readState, "ALL", "PERMANENT", 123);
    }

    @Test
    public void testInvalidConfig() throws Exception {
        ResourceState resourceState = client.read(new RequestContext.Builder().build(), SYSTEM_CONFIG_PATH);
        checkConfig(resourceState, "SECURED", "TEMPORARY", 300);

        ResourceState defaultResourceState = resourceState.getProperty("default", true, ResourceState.class);
        defaultResourceState.putProperty("redirects", "SOME");

        try {
            client.update(new RequestContext.Builder().build(), SYSTEM_CONFIG_PATH, resourceState);
            fail();
        } catch (ResourceException e) {
            //expected
        }

        resourceState = client.read(new RequestContext.Builder().build(), SYSTEM_CONFIG_PATH);
        checkConfig(resourceState, "SECURED", "TEMPORARY", 300);

        defaultResourceState = resourceState.getProperty("default", true, ResourceState.class);
        defaultResourceState.putProperty("redirect-type", "SOMETIMES");

        try {
            client.update(new RequestContext.Builder().build(), SYSTEM_CONFIG_PATH, resourceState);
            fail();
        } catch (ResourceException e) {
            //expected
        }

        resourceState = client.read(new RequestContext.Builder().build(), SYSTEM_CONFIG_PATH);
        checkConfig(resourceState, "SECURED", "TEMPORARY", 300);

        defaultResourceState = resourceState.getProperty("default", true, ResourceState.class);
        defaultResourceState.putProperty("max-age", "NEVER");

        try {
            client.update(new RequestContext.Builder().build(), SYSTEM_CONFIG_PATH, resourceState);
            fail();
        } catch (ResourceException e) {
            //expected
        }

        resourceState = client.read(new RequestContext.Builder().build(), SYSTEM_CONFIG_PATH);
        checkConfig(resourceState, "SECURED", "TEMPORARY", 300);

        defaultResourceState = resourceState.getProperty("default", true, ResourceState.class);
        defaultResourceState.putProperty("max-age", -10);

        try {
            client.update(new RequestContext.Builder().build(), SYSTEM_CONFIG_PATH, resourceState);
            fail();
        } catch (ResourceException e) {
            //expected
        }

        resourceState = client.read(new RequestContext.Builder().build(), SYSTEM_CONFIG_PATH);
        checkConfig(resourceState, "SECURED", "TEMPORARY", 300);
    }

    @Test
    public void testConfigDelete() throws Exception {
        try {
            client.delete(new RequestContext.Builder().build(), SYSTEM_CONFIG_PATH);
            fail();
        } catch (DeleteNotSupportedException e) {
            // expected
        }
    }


    private void checkConfig(ResourceState resourceState, String redirects, String type, int maxAge) throws Exception{
        assertThat(resourceState.getProperty("default")).isNotNull();
        ResourceState defaultRedirect = resourceState.getProperty("default", true, ResourceState.class);
        assertThat(defaultRedirect.getProperty("redirects")).isEqualTo(redirects);
        assertThat(defaultRedirect.getProperty("redirect-type")).isEqualTo(type);
        assertThat(defaultRedirect.getProperty("max-age")).isEqualTo(maxAge);
    }

}
