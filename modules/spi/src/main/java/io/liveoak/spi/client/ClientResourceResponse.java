package io.liveoak.spi.client;

import io.liveoak.spi.state.ResourceState;

/**
 * @author Bob McWhirter
 */
public interface ClientResourceResponse {

    public enum ResponseType {
        OK,

        ERROR,

        NOT_AUTHORIZED,
        NOT_ACCEPTABLE,
        NO_SUCH_RESOURCE,
        RESOURCE_ALREADY_EXISTS,
        CREATE_NOT_SUPPORTED,
        READ_NOT_SUPPORTED,
        UPDATE_NOT_SUPPORTED,
        DELETE_NOT_SUPPORTED,
        INTERNAL_ERROR,
    }

    String path();

    ResponseType responseType();

    ResourceState state();
}
