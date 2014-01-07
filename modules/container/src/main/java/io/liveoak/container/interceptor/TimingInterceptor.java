package io.liveoak.container.interceptor;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import io.liveoak.spi.container.interceptor.DefaultInterceptor;
import io.liveoak.spi.container.interceptor.InboundInterceptorContext;
import org.jboss.logging.Logger;

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
        log.infof("Request took: %d ms", (System.currentTimeMillis() - start));
    }

    private Map<UUID, Long> timings = new ConcurrentHashMap<>();

    private static final Logger log = Logger.getLogger(TimingInterceptor.class);
}
