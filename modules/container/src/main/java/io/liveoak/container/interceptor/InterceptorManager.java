package io.liveoak.container.interceptor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

import io.liveoak.spi.RequestContext;
import io.liveoak.spi.ResourceRequest;
import io.liveoak.spi.ResourceResponse;
import io.liveoak.spi.container.interceptor.Interceptor;
import io.liveoak.spi.state.ResourceState;
import io.netty.channel.ChannelHandlerContext;

/**
 * @author Bob McWhirter
 */
public class InterceptorManager {
    private Comparator<Interceptor> COMPARATOR = (l, r) -> {
        if ( l.priority() > r.priority() ) {
            return -1;
        }

        if ( l.priority() < r.priority() ) {
            return 1;
        }
        return 0;
    };

    public InterceptorManager() {

    }

    public synchronized void register(Interceptor interceptor) {
        this.interceptors.add(interceptor);
        Collections.sort(this.interceptors, COMPARATOR );
    }

    public synchronized void unregister(Interceptor interceptor) {
        this.interceptors.remove(interceptor);
        Collections.sort(this.interceptors, COMPARATOR);
    }

    public void fireInbound(ChannelHandlerContext ctx, ResourceRequest request) {
        InterceptorChain chain = new InterceptorChain( ctx, this.interceptors, request );
        chain.fireInbound();
    }

    public void fireOutbound(ChannelHandlerContext ctx, ResourceResponse response) {
        InterceptorChain chain = new InterceptorChain( ctx, this.interceptors, response );
        chain.fireOutbound();
    }

    public void fireComplete(UUID requestId) {
        for ( Interceptor each : this.interceptors ) {
            each.onComplete( requestId );
        }
    }


    private List<Interceptor> interceptors = new ArrayList<>();
}
