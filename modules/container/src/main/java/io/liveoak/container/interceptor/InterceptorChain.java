package io.liveoak.container.interceptor;

import java.util.ArrayList;
import java.util.List;

import io.liveoak.spi.RequestContext;
import io.liveoak.spi.ResourceResponse;
import io.liveoak.spi.container.interceptor.InboundInterceptorContext;
import io.liveoak.spi.container.interceptor.Interceptor;
import io.liveoak.spi.container.interceptor.OutboundInterceptorContext;
import io.liveoak.spi.state.ResourceState;

/**
 * @author Bob McWhirter
 */
public class InterceptorChain {


    private enum Direction {
        INBOUND,
        OUTBOUND
    }

    ;

    public InterceptorChain(List<Interceptor> interceptors, RequestContext requestContext, ResourceState state) {
        this.interceptors = new ArrayList(interceptors);
        this.requestContext = requestContext;
        this.state = state;
    }

    public RequestContext requestContext() {
        return this.requestContext;
    }

    public ResourceState state() {
        return this.state;
    }

    public void fireInbound() {
        this.direction = Direction.INBOUND;
        this.current = 0;
        fireCurrentInbound();
    }

    protected void fireCurrentInbound() {
        InboundInterceptorContext context = new InboundInterceptorContextImpl(this);
        Interceptor interceptor = this.interceptors.get(this.current);
        try {
            interceptor.onInbound(context);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void fireCurrentOutbound() {
        OutboundInterceptorContext context = new OutboundInterceptorContextImpl(this);
        Interceptor interceptor = this.interceptors.get(this.current);
        try {
            interceptor.onOutbound( context );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void forward() {
        if (this.direction == Direction.INBOUND) {
            ++this.current;
            fireCurrentInbound();
        } else {
            --this.current;
            fireCurrentOutbound();
        }
    }

    public void forward(RequestContext requestContext, ResourceState state) {
        this.requestContext = requestContext;
        this.state = state;
        forward();
    }

    public void forward(ResourceResponse response) {
        this.response = response;
        forward();
    }

    public void replyWith(ResourceResponse response) {
        this.direction = Direction.OUTBOUND;
    }

    private RequestContext requestContext;
    private ResourceState state;
    private ResourceResponse response;

    private final ArrayList<Interceptor> interceptors;
    private int current = 0;
    private Direction direction;


}
