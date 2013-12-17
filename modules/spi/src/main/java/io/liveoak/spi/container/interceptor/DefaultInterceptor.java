package io.liveoak.spi.container.interceptor;

import java.util.UUID;

/**
 * @author Bob McWhirter
 */
public class DefaultInterceptor implements Interceptor {

    public DefaultInterceptor() {
        this( 0 );
    }

    public DefaultInterceptor(int priority) {
        this.priority = priority;
    }

    @Override
    public int priority() {
        return this.priority;
    }

    @Override
    public void onInbound(InboundInterceptorContext context) throws Exception {
        context.forward();
    }

    @Override
    public void onOutbound(OutboundInterceptorContext context) throws Exception {
        context.forward();
    }

    @Override
    public void onComplete(UUID requestId) {
        // nothing;
    }

    private int priority;
}
