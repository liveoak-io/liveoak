package io.liveoak.application.clients;

import com.fasterxml.jackson.databind.JsonNode;

import static org.fest.assertions.Assertions.assertThat;

/**
 * @author Ken Finnigan
 */
public final class Verify {

    public static void appClient(JsonNode node, String id, String type, String[] redirectUris, String[] webOrigins, String[] appRoles) throws Exception {
        assertThat(node.get("id").asText()).isEqualTo(id);
        assertThat(node.get("type").asText()).isEqualTo(type);
        assertThat(node.get("app-key").asText()).isEqualTo("testApp.client." + id);

        verifyArray(redirectUris, node.get("redirect-uris"));
        verifyArray(webOrigins, node.get("web-origins"));
        verifyArray(appRoles, node.get("app-roles"));
    }

    private static void verifyArray(String[] expectedArray, JsonNode actualNode) throws Exception {
        assertThat(actualNode.isArray()).isTrue();
        assertThat(actualNode.size()).isEqualTo(expectedArray.length);
        for (String expected : expectedArray) {
            final boolean[] found = {false};
            actualNode.forEach(item -> {
                if (item.asText().equals(expected)) {
                    found[0] = true;
                }
            });

            assertThat(found[0]).isTrue();
        }
    }
}
