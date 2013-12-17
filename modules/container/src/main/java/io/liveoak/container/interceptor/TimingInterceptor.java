package io.liveoak.container.interceptor;

import java.sql.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import io.liveoak.spi.ResourceRequest;
import io.liveoak.spi.container.interceptor.DefaultInterceptor;
import io.liveoak.spi.container.interceptor.InboundInterceptorContext;
import io.liveoak.spi.container.interceptor.OutboundInterceptorContext;

/**
 * @author Bob McWhirter
 */
public class TimingInterceptor extends DefaultInterceptor {

    @Override
    public void onInbound(InboundInterceptorContext context) throws Exception {
        UUID requestId = context.request().requestId();
        this.timings.put(requestId, System.currentTimeMillis());
        super.onInbound(context);
    }

    @Override
    public void onComplete(UUID requestId) {
        long start = this.timings.remove(requestId);
        System.err.println("Request took: " + (System.currentTimeMillis() - start) + "ms");
    }

    private Map<UUID, Long> timings = new ConcurrentHashMap<>();
}
