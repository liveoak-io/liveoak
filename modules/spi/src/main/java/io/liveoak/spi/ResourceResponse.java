package io.liveoak.spi;

import java.util.UUID;

import io.liveoak.spi.resource.async.Resource;
import io.liveoak.spi.state.ResourceState;

/**
 * @author Bob McWhirter
 */
public interface ResourceResponse {

    public enum ResponseType {
        CREATED,
        READ,
        UPDATED,
        DELETED,
        ERROR,
        MOVED
    }

    ResponseType responseType();
    Resource resource();
    ResourceState state();
    void setState(ResourceState state);
    ResourceRequest inReplyTo();

    UUID requestId();
}
