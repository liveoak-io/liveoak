package io.liveoak.spi.container.interceptor;

import io.liveoak.spi.ResourceRequest;
import io.liveoak.spi.ResourceResponse;

/**
 * @author Bob McWhirter
 */
public interface InboundInterceptorContext extends InterceptorContext {

    ResourceRequest request();

    void forward();
    void forward(ResourceRequest request);

    void replyWith(ResourceResponse response);

}
