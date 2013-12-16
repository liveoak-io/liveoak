package io.liveoak.spi.container.interceptor;

import io.liveoak.spi.ResourceResponse;

/**
 * @author Bob McWhirter
 */
public interface OutboundInterceptorContext extends InterceptorContext {

    void forward();
    void forward(ResourceResponse response);
}
