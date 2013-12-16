package io.liveoak.container.interceptor;

import java.util.List;
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

    public InterceptorManager() {

    }

    public void register(Interceptor interceptor) {
        this.interceptors.add(interceptor);
    }

    public void unregister(Interceptor interceptor) {
        this.interceptors.remove(interceptor);
    }

    public void fireInbound(ChannelHandlerContext ctx, ResourceRequest request) {
        InterceptorChain chain = new InterceptorChain( ctx, this.interceptors, request );
        chain.fireInbound();
    }

    public void fireOutbound(ChannelHandlerContext ctx, ResourceResponse response) {
        InterceptorChain chain = new InterceptorChain( ctx, this.interceptors, response );
        chain.fireOutbound();
    }


    private List<Interceptor> interceptors = new CopyOnWriteArrayList<>();
}
