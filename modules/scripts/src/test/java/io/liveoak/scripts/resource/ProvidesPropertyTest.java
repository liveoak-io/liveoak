package io.liveoak.scripts.resource;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import io.liveoak.spi.MediaType;
import org.junit.Test;

import static io.liveoak.testtools.assertions.Assertions.assertThat;

/**
 * @author Ken Finnigan
 */
public class ProvidesPropertyTest extends BaseResourceTriggeredTestCase {

    @Test
    public void providesChecks() throws Exception {
        // Test #1 - Single provides method
        assertThat(execPost(RESOURCE_SCRIPT_PATH, "{ \"id\": \"single\", \"target-path\": \"targetPath\" }")).hasStatus(201);

        String content = "function preRead(request, libraries) { print('Hello');}";
        assertThat(post(RESOURCE_SCRIPT_PATH + "/single").accept(MediaType.JAVASCRIPT).data(content, MediaType.JAVASCRIPT).execute()).hasStatus(201);
        assertThat(httpResponse.getEntity()).matches(content);

        JsonNode result = getJSON(RESOURCE_SCRIPT_PATH + "/single");

        assertThat(result).isNotNull();
        assertThat(result.get("provides")).isNotNull();
        ArrayNode provides = (ArrayNode) result.get("provides");
        assertThat(provides.size()).isEqualTo(1);
        assertThat(provides.get(0).asText()).isEqualTo("PREREAD");

        // Test #2 - Multiple provides
        assertThat(execPost(RESOURCE_SCRIPT_PATH, "{ \"id\": \"multiple\", \"target-path\": \"targetPath\" }")).hasStatus(201);

        content = "function preRead(request, libraries) { print('Hello');} function postRead(response, libraries) { print('Goodbye');}";
        assertThat(post(RESOURCE_SCRIPT_PATH + "/multiple").accept(MediaType.JAVASCRIPT).data(content, MediaType.JAVASCRIPT).execute()).hasStatus(201);
        assertThat(httpResponse.getEntity()).matches(content);

        result = getJSON(RESOURCE_SCRIPT_PATH + "/multiple");

        assertThat(result).isNotNull();
        assertThat(result.get("provides")).isNotNull();
        provides = (ArrayNode) result.get("provides");
        assertThat(provides.size()).isEqualTo(2);
        provides.forEach(node -> assertThat(node.asText().equals("PREREAD") || node.asText().equals("POSTREAD")).isTrue());

        // Test #3 - non matching method case
        assertThat(execPost(RESOURCE_SCRIPT_PATH, "{ \"id\": \"case\", \"target-path\": \"targetPath\" }")).hasStatus(201);

        content = "function preread(request, libraries) { print('Hello');}";
        assertThat(post(RESOURCE_SCRIPT_PATH + "/case").accept(MediaType.JAVASCRIPT).data(content, MediaType.JAVASCRIPT).execute()).hasStatus(201);
        assertThat(httpResponse.getEntity()).matches(content);

        result = getJSON(RESOURCE_SCRIPT_PATH + "/case");

        assertThat(result).isNotNull();
        assertThat(result.get("provides")).isNotNull();
        provides = (ArrayNode) result.get("provides");
        assertThat(provides.size()).isEqualTo(0);

        // Test #4 - non matching method names
        assertThat(execPost(RESOURCE_SCRIPT_PATH, "{ \"id\": \"count\", \"target-path\": \"targetPath\" }")).hasStatus(201);

        content = "function preReads(request, libraries) { print('Hello');}";
        assertThat(post(RESOURCE_SCRIPT_PATH + "/count").accept(MediaType.JAVASCRIPT).data(content, MediaType.JAVASCRIPT).execute()).hasStatus(201);
        assertThat(httpResponse.getEntity()).matches(content);

        result = getJSON(RESOURCE_SCRIPT_PATH + "/count");

        assertThat(result).isNotNull();
        assertThat(result.get("provides")).isNotNull();
        provides = (ArrayNode) result.get("provides");
        assertThat(provides.size()).isEqualTo(0);

        // Test #5 - check provides list is updated when script updated
        assertThat(execPost(RESOURCE_SCRIPT_PATH, "{ \"id\": \"update\", \"target-path\": \"targetPath\" }")).hasStatus(201);

        content = "function preRead(request, libraries) { print('Hello');}";
        assertThat(post(RESOURCE_SCRIPT_PATH + "/update").accept(MediaType.JAVASCRIPT).data(content, MediaType.JAVASCRIPT).execute()).hasStatus(201);
        assertThat(httpResponse.getEntity()).matches(content);

        result = getJSON(RESOURCE_SCRIPT_PATH + "/update");

        assertThat(result).isNotNull();
        assertThat(result.get("provides")).isNotNull();
        provides = (ArrayNode) result.get("provides");
        assertThat(provides.size()).isEqualTo(1);
        assertThat(provides.get(0).asText()).isEqualTo("PREREAD");

        content = "function postRead(response, libraries) { print('Goodbye');}";
        assertThat(put(RESOURCE_SCRIPT_PATH + "/update/script").accept(MediaType.JAVASCRIPT).data(content, MediaType.JAVASCRIPT).execute()).hasStatus(200);
        assertThat(httpResponse.getEntity()).matches(content);

        result = getJSON(RESOURCE_SCRIPT_PATH + "/update");

        assertThat(result).isNotNull();
        assertThat(result.get("provides")).isNotNull();
        provides = (ArrayNode) result.get("provides");
        assertThat(provides.size()).isEqualTo(1);
        assertThat(provides.get(0).asText()).isEqualTo("POSTREAD");
    }
}
