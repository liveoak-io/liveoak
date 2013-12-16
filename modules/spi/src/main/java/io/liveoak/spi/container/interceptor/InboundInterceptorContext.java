package io.liveoak.spi.container.interceptor;

import io.liveoak.spi.RequestContext;
import io.liveoak.spi.ResourceResponse;
import io.liveoak.spi.state.ResourceState;

/**
 * @author Bob McWhirter
 */
public interface InboundInterceptorContext extends InterceptorContext {

    void forward();
    void forward(RequestContext requestContext, ResourceState state);

    void replyWith(ResourceResponse response);

}
