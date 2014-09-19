package io.liveoak.scripts.resource.resource;

import io.liveoak.scripts.resource.BaseResourceTriggeredTestCase;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.state.ResourceState;
import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 */
public class ReadTestCase extends BaseResourceTriggeredTestCase {

    @Test
    public void testWithNoScripts() throws Exception {
        ResourceState result = client.read( new RequestContext.Builder().build(), RESOURCE_SCRIPT_PATH);

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(RESOURCE_ID);
        assertThat(result.members()).isEmpty();
    }
}
