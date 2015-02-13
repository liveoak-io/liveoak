package io.liveoak.application.clients;

import java.io.File;
import java.util.Iterator;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import io.liveoak.application.clients.extension.ApplicationClientsExtension;
import io.liveoak.spi.LiveOak;
import io.liveoak.testtools.AbstractHTTPResourceTestCase;
import org.fest.assertions.Assertions;
import org.junit.BeforeClass;
import org.junit.Test;

import static io.liveoak.testtools.assertions.Assertions.assertThat;

/**
 * @author Ken Finnigan
 */
public class ApplicationClientsStartupTest extends AbstractHTTPResourceTestCase {

    @BeforeClass
    public static void installExtension() throws Exception {
        system.extensionInstaller().load("application-clients", new ApplicationClientsExtension());

        system.applicationRegistry().createApplication("testApp", "Test Application", new File(ApplicationClientsStartupTest.class.getClassLoader().getResource("testApp").getFile()));

        system.awaitStability();
    }

    @Test
    public void readExistingConfig() throws Exception {
        assertThat(execGet("/admin/applications/testApp/resources/application-clients?fields=*(*)")).hasStatus(200);

        JsonNode result = toJSON(httpResponse.getEntity());
        assertThat(result).isNotNull();

        ArrayNode members = (ArrayNode) result.get(LiveOak.MEMBERS);
        assertThat(members).isNotNull();
        assertThat(members.size()).isEqualTo(2);

        boolean foundHtmlClient = false;
        boolean foundiOSClient = false;
        Iterator<JsonNode> nodeIterator = members.elements();

        while (nodeIterator.hasNext()) {
            JsonNode node = nodeIterator.next();
            if (node.get(LiveOak.ID).asText().equals("html-app-client")) {
                verifyAppClient(node, "html-app-client", "html5", "keycloak-key-html");
                foundHtmlClient = true;
            } else if (node.get(LiveOak.ID).asText().equals("ios-app-client")) {
                verifyAppClient(node, "ios-app-client", "ios", "keycloak-key-ios");
                foundiOSClient = true;
            }
        }

        assertThat(foundHtmlClient).isTrue();
        assertThat(foundiOSClient).isTrue();
    }

    private void verifyAppClient(JsonNode node, String id, String type, String securityKey) throws Exception {
        Assertions.assertThat(node.get("id").asText()).isEqualTo(id);
        Assertions.assertThat(node.get("type").asText()).isEqualTo(type);
        Assertions.assertThat(node.get("security-key").asText()).isEqualTo(securityKey);
    }
}
