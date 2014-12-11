package io.liveoak.scripts.resource.resource;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.liveoak.scripts.resource.BaseResourceTriggeredTestCase;
import org.apache.http.HttpResponse;
import org.fest.assertions.Assertions;
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
        assertThat(execPost(RESOURCE_SCRIPT_PATH, "{ \"id\": \"basic\", \"target-path\": \"/testApp\" }")).hasStatus(201);
        node = (ObjectNode) toJSON(httpResponse.getEntity());
        assertThat(node.get("id").asText()).isEqualTo("basic");
        assertThat(node.get("target-path").asText()).isEqualTo("/testApp");

        node = (ObjectNode) getJSON(RESOURCE_SCRIPT_PATH + "/basic");

        assertThat(node).isNotNull();
        assertThat(node.get("id").asText()).isEqualTo("basic");
        assertThat(node.get("target-path").asText()).isEqualTo("/testApp");

        // Test #3 - script resource create fails with duplicate
        assertThat(execPost(RESOURCE_SCRIPT_PATH, "{ \"id\": \"basic\", \"target-path\": \"/testApp\" }")).hasStatus(406).isDuplicate();

        // Test #4 - delete fails as script resource does not exist
        assertThat(execDelete(RESOURCE_SCRIPT_PATH + "/unknown")).hasStatus(404).hasNoSuchResource();

        // Test #5 - update is converted to create if it doesn't exist
        assertThat(execPut(RESOURCE_SCRIPT_PATH + "/updatetocreate", "{ \"target-path\": \"/testApp\" }")).hasStatus(201);

        // Test #6 - update script resource metadata
        node = (ObjectNode) getJSON(RESOURCE_SCRIPT_PATH + "/basic");
        assertThat(node.get("id").asText()).isEqualTo("basic");
        assertThat(node.get("target-path").asText()).isEqualTo("/testApp");
        assertThat(node.get("priority").asInt()).isEqualTo(5);
        assertThat(node.get("name").isNull()).isTrue();
        assertThat(node.get("description").isNull()).isTrue();
        assertThat(node.get("enabled").asBoolean()).isTrue();

        node.put("name", "some name");
        assertThat(put(RESOURCE_SCRIPT_PATH + "/basic").data(node).execute()).hasStatus(200);

        node = (ObjectNode) getJSON(RESOURCE_SCRIPT_PATH + "/basic");
        assertThat(node.get("id").asText()).isEqualTo("basic");
        assertThat(node.get("target-path").asText()).isEqualTo("/testApp");
        assertThat(node.get("priority").asInt()).isEqualTo(5);
        assertThat(node.get("name").asText()).isEqualTo("some name");
        assertThat(node.get("description").isNull()).isTrue();
        assertThat(node.get("enabled").asBoolean()).isTrue();

        node.put("target-path", "/testApp/foo");
        node.put("enabled", false);
        assertThat(put(RESOURCE_SCRIPT_PATH + "/basic").data(node).execute()).hasStatus(200);

        node = (ObjectNode) getJSON(RESOURCE_SCRIPT_PATH + "/basic");
        assertThat(node.get("id").asText()).isEqualTo("basic");
        assertThat(node.get("target-path").asText()).isEqualTo("/testApp/foo");
        assertThat(node.get("priority").asInt()).isEqualTo(5);
        assertThat(node.get("name").asText()).isEqualTo("some name");
        assertThat(node.get("description").isNull()).isTrue();
        assertThat(node.get("enabled").asBoolean()).isFalse();

        // Test #7 - remove script resource
        assertThat(execDelete(RESOURCE_SCRIPT_PATH + "/updatetocreate")).hasStatus(200);
        assertThat(execGet(RESOURCE_SCRIPT_PATH + "/updatetocreate")).hasStatus(404).hasNoSuchResource();

        // Timeout Tests:
        testCreateTimeout();
        testCreateInvalidTimeout();
        testUpdateTimeout();
        testUpdateInvalidTimeout();

        // Library Tests
        testLibraries();
        testCreateEmptyLibraries();
        testUpdateToEmptyLibraries();

    }

    public void testCreateTimeout() throws Exception {
        String id = "testTimeout";
        HttpResponse createResponse =   execPost(RESOURCE_SCRIPT_PATH, "{ 'id': '" + id + "', 'target-path': '/testApp', 'timeout': 1234 }");
        assertThat(createResponse).hasStatus(201);
        ObjectNode createNode = (ObjectNode) toJSON(createResponse.getEntity());
        Assertions.assertThat(createNode.get("id").asText()).isEqualTo(id);
        assertThat(createNode.get("timeout").isNumber());
        Assertions.assertThat(createNode.get("timeout").asInt()).isEqualTo(1234);

        ObjectNode readNode = (ObjectNode) getJSON(RESOURCE_SCRIPT_PATH + "/" + id);

        Assertions.assertThat(readNode).isNotNull();
        Assertions.assertThat(readNode.get("id").asText()).isEqualTo(id);
        Assertions.assertThat(createNode.get("timeout").asInt()).isEqualTo(1234);
    }

    public void testUpdateTimeout() throws Exception {
        //testCreateTimeout();
        String id = "testTimeout";
        HttpResponse updateResponse = execPut(RESOURCE_SCRIPT_PATH + "/" + id, "{ 'id': '" + id + "', 'target-path': '/testApp', 'timeout': 4567 }");
        assertThat(updateResponse).hasStatus(200);
        ObjectNode createNode = (ObjectNode) toJSON(updateResponse.getEntity());
        Assertions.assertThat(createNode.get("id").asText()).isEqualTo(id);
        assertThat(createNode.get("timeout").isNumber());
        Assertions.assertThat(createNode.get("timeout").asInt()).isEqualTo(4567);

        ObjectNode readNode = (ObjectNode) getJSON(RESOURCE_SCRIPT_PATH + "/" + id);

        Assertions.assertThat(readNode).isNotNull();
        Assertions.assertThat(readNode.get("id").asText()).isEqualTo(id);
        Assertions.assertThat(createNode.get("timeout").asInt()).isEqualTo(4567);
    }

    public void testCreateInvalidTimeout() throws Exception {
        String id = "testInvalidTimeoutCreate";
        HttpResponse createResponse =   execPost(RESOURCE_SCRIPT_PATH, "{ 'id': '" + id + "', 'target-path': '/testApp', 'timeout': -1234 }");
        assertThat(createResponse).hasStatus(406);
        ObjectNode createNode = (ObjectNode) toJSON(createResponse.getEntity());
        assertThat(createNode.get("error-type").asText()).isEqualTo("NOT_ACCEPTABLE");
        assertThat(createNode.get("message").asText()).isEqualTo("'timeout' must be a positive number.");
    }

    public void testUpdateInvalidTimeout() throws Exception {
        //testCreateTimeout();
        String id = "testInvalidTimeoutUpdate";
        HttpResponse updateResponse = execPut(RESOURCE_SCRIPT_PATH + "/" + id, "{ 'id': '" + id + "', 'target-path': '/testApp', 'timeout': 'foo' }");
        assertThat(updateResponse).hasStatus(406);
        ObjectNode createNode = (ObjectNode) toJSON(updateResponse.getEntity());
        assertThat(createNode.get("error-type").asText()).isEqualTo("NOT_ACCEPTABLE");
        assertThat(createNode.get("message").asText()).isEqualTo("Invalid property type. The property named 'timeout' expects a type of Integer");
    }

    //@Test
    public void testCreateEmptyLibraries() throws Exception {
        String id = "testCreateEmptyLibraries";
        HttpResponse createResponse =   execPost(RESOURCE_SCRIPT_PATH, "{ 'id': '" + id + "', 'target-path': '/testApp'}");
        assertThat(createResponse).hasStatus(201);
        ObjectNode createNode = (ObjectNode) toJSON(createResponse.getEntity());
        Assertions.assertThat(createNode.get("id").asText()).isEqualTo(id);
        assertThat(createNode.get("libraries").isArray());
        assertThat(createNode.get("libraries").size()).isEqualTo(0);

        ObjectNode readNode = (ObjectNode) getJSON(RESOURCE_SCRIPT_PATH + "/" + id);

        assertThat(readNode).isNotNull();
        assertThat(readNode.get("id").asText()).isEqualTo(id);
        assertThat(readNode.get("libraries").isArray()).isTrue();
        assertThat(readNode.get("libraries").size()).isEqualTo(0);
    }

    //@Test
    public void testLibraries() throws Exception {
        String id = "testLibraries";
        HttpResponse createResponse =   execPost(RESOURCE_SCRIPT_PATH, "{ 'id': '" + id + "', 'target-path': '/testApp', 'libraries': ['foo', 'bar']}");
        assertThat(createResponse).hasStatus(201);
        ObjectNode createNode = (ObjectNode) toJSON(createResponse.getEntity());
        Assertions.assertThat(createNode.get("id").asText()).isEqualTo(id);
        assertThat(createNode.get("libraries").isArray());
        assertThat(createNode.get("libraries").size()).isEqualTo(2);
        assertThat(createNode.get("libraries").get(0).textValue()).isEqualTo("foo");
        assertThat(createNode.get("libraries").get(1).textValue()).isEqualTo("bar");

        ObjectNode readNode = (ObjectNode) getJSON(RESOURCE_SCRIPT_PATH + "/" + id);
        assertThat(readNode).isNotNull();
        assertThat(readNode.get("id").asText()).isEqualTo(id);
        assertThat(readNode.get("libraries").isArray()).isTrue();
        assertThat(readNode.get("libraries").size()).isEqualTo(2);
        assertThat(readNode.get("libraries").get(0).textValue()).isEqualTo("foo");
        assertThat(readNode.get("libraries").get(1).textValue()).isEqualTo("bar");
    }

    //@Test
    public void testUpdateToEmptyLibraries() throws Exception {
        //testLibraries();
        String id = "testLibraries";

        HttpResponse updateResponse = execPut(RESOURCE_SCRIPT_PATH + "/" + id, "{ 'id': '" + id + "', 'target-path': '/testApp'}");
        assertThat(updateResponse).hasStatus(200);

        ObjectNode updateNode = (ObjectNode) toJSON(updateResponse.getEntity());
        Assertions.assertThat(updateNode.get("id").asText()).isEqualTo(id);
        assertThat(updateNode.get("libraries").isArray());
        assertThat(updateNode.get("libraries").size()).isEqualTo(0);

        ObjectNode readNode = (ObjectNode) getJSON(RESOURCE_SCRIPT_PATH + "/" + id);

        assertThat(readNode).isNotNull();
        assertThat(readNode.get("id").asText()).isEqualTo(id);
        assertThat(readNode.get("libraries").isArray()).isTrue();
        assertThat(readNode.get("libraries").size()).isEqualTo(0);
    }

    @Test
    public void testTargetPath() throws Exception {
        String id = "testTargetPath";

        //
        HttpResponse updateResponse = execPut(RESOURCE_SCRIPT_PATH + "/" + id, "{ 'id': '" + id + "', 'target-path': '/testApp'}");
        assertThat(updateResponse).hasStatus(201);
        ObjectNode updateNode = (ObjectNode) toJSON(updateResponse.getEntity());
        assertThat(updateNode.get("target-path").asText()).isEqualTo("/testApp");


        //
        updateResponse = execPut(RESOURCE_SCRIPT_PATH + "/" + id, "{ 'id': '" + id + "', 'target-path': '/testApp*'}");
        assertThat(updateResponse).hasStatus(200);
        updateNode = (ObjectNode) toJSON(updateResponse.getEntity());
        assertThat(updateNode.get("target-path").asText()).isEqualTo("/testApp*");

        //
        updateResponse = execPut(RESOURCE_SCRIPT_PATH + "/" + id, "{ 'id': '" + id + "', 'target-path': '/testApp**'}");
        assertThat(updateResponse).hasStatus(200);
        updateNode = (ObjectNode) toJSON(updateResponse.getEntity());
        assertThat(updateNode.get("target-path").asText()).isEqualTo("/testApp**");

        //
        updateResponse = execPut(RESOURCE_SCRIPT_PATH + "/" + id, "{ 'id': '" + id + "', 'target-path': '/testApp/*'}");
        assertThat(updateResponse).hasStatus(200);
        updateNode = (ObjectNode) toJSON(updateResponse.getEntity());
        assertThat(updateNode.get("target-path").asText()).isEqualTo("/testApp/*");

        //
        updateResponse = execPut(RESOURCE_SCRIPT_PATH + "/" + id, "{ 'id': '" + id + "', 'target-path': '/testApp/**'}");
        assertThat(updateResponse).hasStatus(200);
        updateNode = (ObjectNode) toJSON(updateResponse.getEntity());
        assertThat(updateNode.get("target-path").asText()).isEqualTo("/testApp/**");

        //
        updateResponse = execPut(RESOURCE_SCRIPT_PATH + "/" + id, "{ 'id': '" + id + "', 'target-path': '/testApp/foo'}");
        assertThat(updateResponse).hasStatus(200);
        updateNode = (ObjectNode) toJSON(updateResponse.getEntity());
        assertThat(updateNode.get("target-path").asText()).isEqualTo("/testApp/foo");

        //
        updateResponse = execPut(RESOURCE_SCRIPT_PATH + "/" + id, "{ 'id': '" + id + "', 'target-path': '/testApp/foo*'}");
        assertThat(updateResponse).hasStatus(200);
        updateNode = (ObjectNode) toJSON(updateResponse.getEntity());
        assertThat(updateNode.get("target-path").asText()).isEqualTo("/testApp/foo*");

        //
        updateResponse = execPut(RESOURCE_SCRIPT_PATH + "/" + id, "{ 'id': '" + id + "', 'target-path': '/testApp/foo**'}");
        assertThat(updateResponse).hasStatus(200);
        updateNode = (ObjectNode) toJSON(updateResponse.getEntity());
        assertThat(updateNode.get("target-path").asText()).isEqualTo("/testApp/foo**");

        //
        updateResponse = execPut(RESOURCE_SCRIPT_PATH + "/" + id, "{ 'id': '" + id + "', 'target-path': '/testApp**'}");
        assertThat(updateResponse).hasStatus(200);
        updateNode = (ObjectNode) toJSON(updateResponse.getEntity());
        assertThat(updateNode.get("target-path").asText()).isEqualTo("/testApp**");

        //
        updateResponse = execPut(RESOURCE_SCRIPT_PATH + "/" + id, "{ 'id': '" + id + "', 'target-path': 'testApp'}");
        assertThat(updateResponse).hasStatus(406);

        //
        updateResponse = execPut(RESOURCE_SCRIPT_PATH + "/" + id, "{ 'id': '" + id + "', 'target-path': '/testApp/'}");
        assertThat(updateResponse).hasStatus(406);

        //
        updateResponse = execPut(RESOURCE_SCRIPT_PATH + "/" + id, "{ 'id': '" + id + "', 'target-path': '/foo*'}");
        assertThat(updateResponse).hasStatus(406);

        //
        updateResponse = execPut(RESOURCE_SCRIPT_PATH + "/" + id, "{ 'id': '" + id + "', 'target-path': '/testApp/bar/'}");
        assertThat(updateResponse).hasStatus(406);

        //
        updateResponse = execPut(RESOURCE_SCRIPT_PATH + "/" + id, "{ 'id': '" + id + "', 'target-path': '/foo/bar*'}");
        assertThat(updateResponse).hasStatus(406);

    }
}
