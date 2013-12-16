package io.liveoak.spi.container.interceptor;

/**
 * @author Bob McWhirter
 */
public interface Interceptor {

    void onInbound(InboundInterceptorContext context) throws Exception;

    void onOutbound(OutboundInterceptorContext context) throws Exception;
}
