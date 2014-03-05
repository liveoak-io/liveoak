package io.liveoak.interceptor.timing;

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

    public TimingInterceptor(String chainName) {
        this.chainName = chainName;
    }

    @Override
    public void onInbound(InboundInterceptorContext context) throws Exception {
        UUID requestId = context.request().requestId();
        this.timings.put(requestId, System.currentTimeMillis());
        super.onInbound(context);
    }

    @Override
    public void onComplete(UUID requestId) {
        long start = this.timings.remove(requestId);
        log.infof("%s request took: %d ms", chainName, (System.currentTimeMillis() - start));
    }

    private final Map<UUID, Long> timings = new ConcurrentHashMap<>();
    private final String chainName;

    private static final Logger log = Logger.getLogger(TimingInterceptor.class);
}
