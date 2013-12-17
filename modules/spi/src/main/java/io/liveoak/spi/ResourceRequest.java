package io.liveoak.spi;

import java.util.UUID;

import io.liveoak.spi.state.ResourceState;

/**
 * @author Bob McWhirter
 */
public interface ResourceRequest {

    UUID requestId();
    RequestType requestType();
    ResourcePath resourcePath();
    ResourceState state();
    RequestContext requestContext();
    MediaTypeMatcher mediaTypeMatcher();
}
