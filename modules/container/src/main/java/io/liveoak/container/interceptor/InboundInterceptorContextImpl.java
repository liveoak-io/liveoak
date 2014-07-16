package io.liveoak.container.interceptor;

import io.liveoak.spi.ResourceRequest;
import io.liveoak.spi.ResourceResponse;
import io.liveoak.spi.container.interceptor.InboundInterceptorContext;

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
    public void forward(ResourceRequest request) {
        this.chain.forward(request);
    }

    @Override
    public void replyWith(ResourceResponse response) {
        this.chain.replyWith(response);
    }

    @Override
    public ResourceRequest request() {
        return this.chain.request();
    }

    private InterceptorChain chain;
}
