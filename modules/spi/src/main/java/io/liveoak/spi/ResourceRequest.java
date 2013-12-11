package io.liveoak.spi;

import io.liveoak.spi.state.ResourceState;

/**
 * @author Bob McWhirter
 */
public interface ResourceRequest {

    ResourcePath resourcePath();
    ResourceState state();
    RequestContext requestContext();
    MediaTypeMatcher mediaTypeMatcher();
}
