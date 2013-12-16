package io.liveoak.spi.container.interceptor;

/**
 * @author Bob McWhirter
 */
public class DefaultInterceptor implements Interceptor {
    @Override
    public void onInbound(InboundInterceptorContext context) throws Exception {
        context.forward();
    }

    @Override
    public void onOutbound(OutboundInterceptorContext context) throws Exception {
        context.forward();
    }
}
