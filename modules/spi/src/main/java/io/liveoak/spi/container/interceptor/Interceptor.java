package io.liveoak.spi.container.interceptor;

import java.util.UUID;

/**
 * @author Bob McWhirter
 */
public interface Interceptor {

    int priority();

    void onInbound(InboundInterceptorContext context) throws Exception;

    void onOutbound(OutboundInterceptorContext context) throws Exception;

    void onComplete(UUID requestId);
}
