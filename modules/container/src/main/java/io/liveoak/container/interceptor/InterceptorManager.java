package io.liveoak.container.interceptor;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import io.liveoak.spi.RequestContext;
import io.liveoak.spi.container.interceptor.Interceptor;
import io.liveoak.spi.state.ResourceState;

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

    public void fireInbound(RequestContext context, ResourceState state) {
        InterceptorChain chain = new InterceptorChain( this.interceptors, context, state );
        chain.fireInbound();
    }


    private List<Interceptor> interceptors = new CopyOnWriteArrayList<>();
}
