package io.liveoak.client.impl;

import io.liveoak.spi.client.ClientResourceResponse;
import io.liveoak.spi.state.ResourceState;

/**
 * @author Bob McWhirter
 */
public class ClientResourceResponseImpl implements ClientResourceResponse {

    public ClientResourceResponseImpl(ResponseType responseType, String path, ResourceState state) {
        this.responseType = responseType;
        this.path = path;
        this.state = state;
    }

    @Override
    public String path() {
        return this.path;
    }

    @Override
    public ResponseType responseType() {
        return this.responseType;
    }

    @Override
    public ResourceState state() {
        return this.state;
    }

    public String toString() {
        return "[ClientResourceResponseImpl: type=" + this.responseType + "; state=" + this.state + "]";
    }

    private ResponseType responseType;
    private String path;
    private ResourceState state;
}
