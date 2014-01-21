package io.liveoak.container.extension.reconfig;

import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * @author Bob McWhirter
 */
public class MockClient {

    public MockClient(ObjectNode config) {
        this.config = config;
    }

    public ObjectNode config() {
        return this.config;
    }

    private ObjectNode config;
}
