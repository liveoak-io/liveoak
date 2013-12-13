package io.liveoak.client;

import java.util.function.Consumer;

import io.liveoak.spi.ResourceRequest;
import io.liveoak.spi.client.ClientResourceResponse;

/**
 * @author Bob McWhirter
 */
public class ClientRequest {


    public ClientRequest(ResourceRequest resourceRequest, Consumer<ClientResourceResponse> handler) {
        this.resourceRequest = resourceRequest;
        this.handler = handler;
    }


    public ResourceRequest resourceRequest() {
        return this.resourceRequest;
    }

    public Consumer<ClientResourceResponse> handler() {
        return this.handler;
    }

    private final ResourceRequest resourceRequest;
    private final Consumer<ClientResourceResponse> handler;

}
