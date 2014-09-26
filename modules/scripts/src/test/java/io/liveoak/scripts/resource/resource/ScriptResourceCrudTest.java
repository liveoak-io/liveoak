package io.liveoak.scripts.resource.resource;

import com.fasterxml.jackson.databind.JsonNode;
import io.liveoak.scripts.resource.BaseResourceTriggeredTestCase;
import io.liveoak.spi.ResourceAlreadyExistsException;
import io.liveoak.spi.ResourceNotFoundException;
import org.junit.Test;

import static io.liveoak.testtools.assertions.Assertions.assertThat;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 * @author Ken Finnigan
 */
public class ScriptResourceCrudTest extends BaseResourceTriggeredTestCase {

    @Test
    public void restCrud() throws Exception {
        // Test #1 - extension installed but no script
        JsonNode node = getJSON(RESOURCE_SCRIPT_PATH);

        assertThat(node).isNotNull();
        assertThat(node.get("id").asText()).isEqualTo(RESOURCE_ID);
        assertThat(node.get("members")).isNull();

        // Test #2 - extension installed with script resource
        assertThat(execPost(RESOURCE_SCRIPT_PATH, "{ \"id\": \"basic\", \"target-path\": \"targetPath\" }")).hasStatus(201);
        node = toJSON(httpResponse.getEntity());
        assertThat(node.get("id").asText()).isEqualTo("basic");
        assertThat(node.get("target-path").asText()).isEqualTo("targetPath");

        node = getJSON(RESOURCE_SCRIPT_PATH + "/basic");

        assertThat(node).isNotNull();
        assertThat(node.get("id").asText()).isEqualTo("basic");
        assertThat(node.get("target-path").asText()).isEqualTo("targetPath");

        // Test #3 - script resource create fails with duplicate
        assertThat(execPost(RESOURCE_SCRIPT_PATH, "{ \"id\": \"basic\", \"target-path\": \"targetPath\" }")).hasStatus(406).isDuplicate();
    }

//    @Test(expected = ResourceAlreadyExistsException.class)
    public void installFailsWithDuplicate() throws Exception {

    }

//    @Test
    public void updateResourceMetadata() throws Exception {

    }

//    @Test
    public void resourceRemoved() throws Exception {

    }

//    @Test(expected = ResourceNotFoundException.class)
    public void deleteFailsAsResourceDoesNotExist() throws Exception {

    }

//    @Test(expected = ResourceNotFoundException.class)
    public void updateFailsAsResourceDoesNotExist() throws Exception {

    }
}
