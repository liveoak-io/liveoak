package io.liveoak.container.interceptor;

import io.liveoak.spi.RequestContext;
import io.liveoak.spi.ResourceRequest;
import io.liveoak.spi.ResourceResponse;
import io.liveoak.spi.container.interceptor.OutboundInterceptorContext;
import io.liveoak.spi.state.ResourceState;

/**
 * @author Bob McWhirter
 */
public class OutboundInterceptorContextImpl implements OutboundInterceptorContext {

    public OutboundInterceptorContextImpl(InterceptorChain chain) {
        this.chain = chain;
    }

    @Override
    public void forward() {
        this.chain.forward();
    }

    @Override
    public void forward(ResourceResponse response) {
        this.chain.forward( response );
    }

    @Override
    public ResourceRequest request() {
        return this.chain.request();
    }

    @Override
    public ResourceResponse response() {
        return this.chain.response();
    }

    private final InterceptorChain chain;

}
