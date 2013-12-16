package io.liveoak.container.interceptor;

import java.sql.Date;
import java.util.HashMap;
import java.util.Map;
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
        this.timings.put(context.request(), System.currentTimeMillis());
        super.onInbound(context);
    }

    @Override
    public void onOutbound(OutboundInterceptorContext context) throws Exception {
        long start = this.timings.remove(context.request());
        System.err.println("Request took: " + (System.currentTimeMillis() - start) + "ms");
        super.onOutbound(context);
    }

    private Map<ResourceRequest, Long> timings = new ConcurrentHashMap<>();
}
