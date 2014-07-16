package io.liveoak.container.interceptor;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

import io.liveoak.container.interceptor.extension.InterceptorConfigEntry;
import io.liveoak.container.interceptor.extension.InterceptorsConfig;
import io.liveoak.spi.ResourcePath;
import io.liveoak.spi.ResourceRequest;
import io.liveoak.spi.ResourceResponse;
import io.liveoak.spi.container.interceptor.Interceptor;
import io.liveoak.spi.container.interceptor.InterceptorManager;
import io.liveoak.spi.state.ResourceState;
import io.netty.channel.ChannelHandlerContext;
import org.jboss.logging.Logger;

/**
 * @author Bob McWhirter
 */
public class InterceptorManagerImpl implements InterceptorManager {

    private static final Logger log = Logger.getLogger(InterceptorManagerImpl.class);

    @Override
    public synchronized void register(String interceptorName, Interceptor interceptor) {
        this.interceptors.put(interceptorName, interceptor);
    }

    @Override
    public synchronized void unregister(Interceptor interceptor) {
        for (Map.Entry<String, Interceptor> entry : interceptors.entrySet()) {
            if (entry.getValue().equals(interceptor)) {
                interceptors.remove(entry.getKey());
                break;
            }
        }
    }

    @Override
    public void setInterceptorsConfig(ResourceState resourceState) throws IOException {
        InterceptorsConfig result = InterceptorsConfig.createConfigFromResourceState(resourceState);
        this.interceptorsConfig.set(result);
        if (log.isDebugEnabled()) {
            log.debugf("Interceptors configuration updated: %s", interceptorsConfig);
        }
    }

    @Override
    public ResourceState getInterceptorsConfig() throws IOException {
        return this.interceptorsConfig.get().getConfigAsResourceState();
    }


    @Override
    public void fireInbound(String chainName, ChannelHandlerContext ctx, ResourceRequest request) {
        List<Interceptor> interceptors = getInterceptors(chainName, request);
        InterceptorChain chain = new InterceptorChain(ctx, interceptors, request);
        chain.fireInbound();
    }

    @Override
    public void fireOutbound(String chainName, ChannelHandlerContext ctx, ResourceResponse response) {
        List<Interceptor> interceptors = getInterceptors(chainName, response.inReplyTo());
        InterceptorChain chain = new InterceptorChain(ctx, interceptors, response);
        chain.fireOutbound();
    }

    @Override
    public void fireComplete(String chainName, UUID requestId) {
        List<Interceptor> interceptors = getInterceptors(chainName, null);
        for (Interceptor each : interceptors) {
            each.onComplete(requestId);
        }
    }

    private List<Interceptor> getInterceptors(String chainName, ResourceRequest request) {
        List<Interceptor> result = new LinkedList<>();

        List<InterceptorConfigEntry> chainConfigEntries = interceptorsConfig.get().getChainConfig(chainName);
        for (InterceptorConfigEntry configEntry : chainConfigEntries) {

            String interceptorName = configEntry.getInterceptorName();
            Interceptor interceptor = interceptors.get(interceptorName);
            if (interceptor == null) {
                log.warnf("No interceptor under key '%s'", interceptorName);
                continue;
            }

            // Verify resourcePath matches
            if (configEntry.getResourcePathMapping() != null && request != null) {
                ResourcePath interceptorResPath = new ResourcePath(configEntry.getResourcePathMapping());
                if (!interceptorResPath.isParentOf(request.resourcePath())) {
                    continue;
                }
            }

            // Verify requestType matches
            if (configEntry.getRequestTypeMapping() != null && request != null) {
                if (!request.requestType().matches(configEntry.getRequestTypeMapping())) {
                    continue;
                }
            }

            result.add(interceptor);
        }

        return result;
    }

    private AtomicReference<InterceptorsConfig> interceptorsConfig = new AtomicReference<>(new InterceptorsConfig());
    private Map<String, Interceptor> interceptors = new ConcurrentHashMap<>();
}
