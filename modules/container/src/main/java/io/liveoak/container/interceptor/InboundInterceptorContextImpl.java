package io.liveoak.container.interceptor;

import io.liveoak.spi.RequestContext;
import io.liveoak.spi.ResourceResponse;
import io.liveoak.spi.container.interceptor.InboundInterceptorContext;
import io.liveoak.spi.state.ResourceState;

/**
 * @author Bob McWhirter
 */
public class InboundInterceptorContextImpl implements InboundInterceptorContext {

    public InboundInterceptorContextImpl(InterceptorChain chain) {
        this.chain = chain;
    }

    @Override
    public void forward() {
        this.chain.forward();
    }

    @Override
    public void forward(RequestContext requestContext, ResourceState state) {
        this.chain.forward( requestContext, state );
    }

    @Override
    public void replyWith(ResourceResponse response) {
        this.chain.replyWith(response);
    }

    @Override
    public RequestContext requestContext() {
        return this.chain.requestContext();
    }

    @Override
    public ResourceState state() {
        return this.chain.state();
    }

    private InterceptorChain chain;
}
