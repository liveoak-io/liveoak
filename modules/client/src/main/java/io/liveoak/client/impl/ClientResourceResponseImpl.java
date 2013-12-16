package io.liveoak.client.impl;

import io.liveoak.client.ClientRequest;
import io.liveoak.spi.ResourceRequest;
import io.liveoak.spi.client.ClientResourceResponse;
import io.liveoak.spi.state.ResourceState;

/**
 * @author Bob McWhirter
 */
public class ClientResourceResponseImpl implements ClientResourceResponse {

    public ClientResourceResponseImpl(ResourceRequest inReplyTo, ResponseType responseType, String path, ResourceState state) {
        this.inReplyTo = inReplyTo;
        this.responseType = responseType;
        this.path = path;
        this.state = state;
    }

    public ResourceRequest inReplyTo() {
        return this.inReplyTo;
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

    private ResourceRequest inReplyTo;
    private ResponseType responseType;
    private String path;
    private ResourceState state;
}
