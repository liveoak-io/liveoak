package io.liveoak.application.clients;

import java.io.IOException;

import io.liveoak.keycloak.KeycloakConfig;
import io.liveoak.keycloak.client.DirectAccessClient;

/**
 * @author Ken Finnigan
 */
public class MockDirectAccessClient extends DirectAccessClient {
    public MockDirectAccessClient(KeycloakConfig config) {
        super(config);
    }

    @Override
    public String accessToken() throws IOException {
        return "";
    }

    @Override
    public void close() {
    }

    @Override
    public void shutdown() {
    }
}
