package io.liveoak.scripts.resource;

import io.liveoak.spi.NotAcceptableException;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.state.ResourceState;
import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;

/**
 * @author Ken Finnigan
 */
public class PriorityPropertyTest extends BaseResourceTriggeredTestCase {

    @Test
    public void ensureScriptCreatedWithDefaultValue() throws Exception {
        ResourceState result = client.create(new RequestContext.Builder().build(), RESOURCE_SCRIPT_PATH, new MetadataState("default", "targetPath").build());
        assertThat(result).isNotNull();
        assertThat(result.getProperty("priority")).isEqualTo(5);
    }

    @Test(expected = NotAcceptableException.class)
    public void checkNegativePriorityFails() throws Exception {
        client.create(new RequestContext.Builder().build(), RESOURCE_SCRIPT_PATH, new MetadataState("negative", "targetPath").priority(-1).build());
    }

    @Test(expected = NotAcceptableException.class)
    public void checkZeroPriorityFails() throws Exception {
        client.create(new RequestContext.Builder().build(), RESOURCE_SCRIPT_PATH, new MetadataState("zero", "targetPath").priority(0).build());
    }

    @Test(expected = NotAcceptableException.class)
    public void checkPriorityGreaterThanTenFails() throws Exception {
        client.create(new RequestContext.Builder().build(), RESOURCE_SCRIPT_PATH, new MetadataState("toolarge", "targetPath").priority(11).build());
    }

    @Test
    public void checkValidPriority() throws Exception {
        ResourceState result = client.create(new RequestContext.Builder().build(), RESOURCE_SCRIPT_PATH, new MetadataState("valid", "targetPath").priority(7).build());
        assertThat(result).isNotNull();
        assertThat(result.getProperty("priority")).isEqualTo(7);
    }

    @Test(expected = NotAcceptableException.class)
    public void checkUpdateFromValidFails() throws Exception {
        ResourceState result = client.create(new RequestContext.Builder().build(), RESOURCE_SCRIPT_PATH, new MetadataState("invalidupdate", "targetPath").priority(2).build());
        assertThat(result).isNotNull();
        assertThat(result.getProperty("priority")).isEqualTo(2);

        result.putProperty("priority", 12);
        client.update(new RequestContext.Builder().build(), RESOURCE_SCRIPT_PATH + "/invalidupdate", result);
    }
}
