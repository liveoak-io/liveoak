package io.liveoak.container.interceptor;

import io.liveoak.spi.RequestContext;
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
    public RequestContext requestContext() {
        return this.chain.requestContext();
    }

    @Override
    public ResourceState state() {
        return this.chain.state();
    }

    private final InterceptorChain chain;

}
