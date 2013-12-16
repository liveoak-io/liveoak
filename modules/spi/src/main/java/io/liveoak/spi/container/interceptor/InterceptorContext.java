package io.liveoak.spi.container.interceptor;

import io.liveoak.spi.RequestContext;
import io.liveoak.spi.state.ResourceState;

/**
 * @author Bob McWhirter
 */
public interface InterceptorContext {

    RequestContext requestContext();
    ResourceState state();
}
