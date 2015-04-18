package io.liveoak.container;

import java.io.IOException;

import io.liveoak.security.client.DirectAccessClient;

/**
 * @author Ken Finnigan
 */
public class MockDirectAccessClient extends DirectAccessClient {
    public MockDirectAccessClient() {
        super("");
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
