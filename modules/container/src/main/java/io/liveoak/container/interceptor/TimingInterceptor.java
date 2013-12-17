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
    public void onOutbound(OutboundInterceptorContext context) throws Exception {
        UUID requestId = context.request().requestId();
        Long start = this.timings.remove(requestId);
        if (start != null) {
            System.err.println("Request took: " + (System.currentTimeMillis() - start) + "ms");
        }
        super.onOutbound(context);
    }

    private Map<UUID, Long> timings = new ConcurrentHashMap<>();
}
