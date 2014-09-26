package io.liveoak.scripts.resource.resource;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.liveoak.scripts.resource.BaseResourceTriggeredTestCase;
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
        ObjectNode node = (ObjectNode) getJSON(RESOURCE_SCRIPT_PATH);

        assertThat(node).isNotNull();
        assertThat(node.get("id").asText()).isEqualTo(RESOURCE_ID);
        assertThat(node.get("members")).isNull();

        // Test #2 - extension installed with script resource
        assertThat(execPost(RESOURCE_SCRIPT_PATH, "{ \"id\": \"basic\", \"target-path\": \"targetPath\" }")).hasStatus(201);
        node = (ObjectNode) toJSON(httpResponse.getEntity());
        assertThat(node.get("id").asText()).isEqualTo("basic");
        assertThat(node.get("target-path").asText()).isEqualTo("targetPath");

        node = (ObjectNode) getJSON(RESOURCE_SCRIPT_PATH + "/basic");

        assertThat(node).isNotNull();
        assertThat(node.get("id").asText()).isEqualTo("basic");
        assertThat(node.get("target-path").asText()).isEqualTo("targetPath");

        // Test #3 - script resource create fails with duplicate
        assertThat(execPost(RESOURCE_SCRIPT_PATH, "{ \"id\": \"basic\", \"target-path\": \"targetPath\" }")).hasStatus(406).isDuplicate();

        // Test #4 - delete fails as script resource does not exist
        assertThat(execDelete(RESOURCE_SCRIPT_PATH + "/unknown")).hasStatus(404).hasNoSuchResource();

        // Test #5 - update is converted to create if it doesn't exist
        assertThat(execPut(RESOURCE_SCRIPT_PATH + "/updatetocreate", "{ \"target-path\": \"targetPath\" }")).hasStatus(201);

        // Test #6 - update script resource metadata
        node = (ObjectNode) getJSON(RESOURCE_SCRIPT_PATH + "/basic");
        assertThat(node.get("id").asText()).isEqualTo("basic");
        assertThat(node.get("target-path").asText()).isEqualTo("targetPath");
        assertThat(node.get("priority").asInt()).isEqualTo(5);
        assertThat(node.get("name").isNull()).isTrue();
        assertThat(node.get("description").isNull()).isTrue();
        assertThat(node.get("enabled").asBoolean()).isTrue();

        node.put("name", "some name");
        assertThat(put(RESOURCE_SCRIPT_PATH + "/basic").data(node).execute()).hasStatus(200);

        node = (ObjectNode) getJSON(RESOURCE_SCRIPT_PATH + "/basic");
        assertThat(node.get("id").asText()).isEqualTo("basic");
        assertThat(node.get("target-path").asText()).isEqualTo("targetPath");
        assertThat(node.get("priority").asInt()).isEqualTo(5);
        assertThat(node.get("name").asText()).isEqualTo("some name");
        assertThat(node.get("description").isNull()).isTrue();
        assertThat(node.get("enabled").asBoolean()).isTrue();

        node.put("target-path", "newTargetPath");
        node.put("enabled", false);
        assertThat(put(RESOURCE_SCRIPT_PATH + "/basic").data(node).execute()).hasStatus(200);

        node = (ObjectNode) getJSON(RESOURCE_SCRIPT_PATH + "/basic");
        assertThat(node.get("id").asText()).isEqualTo("basic");
        assertThat(node.get("target-path").asText()).isEqualTo("newTargetPath");
        assertThat(node.get("priority").asInt()).isEqualTo(5);
        assertThat(node.get("name").asText()).isEqualTo("some name");
        assertThat(node.get("description").isNull()).isTrue();
        assertThat(node.get("enabled").asBoolean()).isFalse();

        // Test #7 - remove script resource
        assertThat(execDelete(RESOURCE_SCRIPT_PATH + "/updatetocreate")).hasStatus(200);
        assertThat(execGet(RESOURCE_SCRIPT_PATH + "/updatetocreate")).hasStatus(404).hasNoSuchResource();
    }
}
