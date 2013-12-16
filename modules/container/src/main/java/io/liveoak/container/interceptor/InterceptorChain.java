package io.liveoak.container.interceptor;

import java.util.ArrayList;
import java.util.List;

import io.liveoak.spi.ResourceRequest;
import io.liveoak.spi.ResourceResponse;
import io.liveoak.spi.container.interceptor.InboundInterceptorContext;
import io.liveoak.spi.container.interceptor.Interceptor;
import io.liveoak.spi.container.interceptor.OutboundInterceptorContext;
import io.netty.channel.ChannelHandlerContext;

/**
 * @author Bob McWhirter
 */
public class InterceptorChain {


    private enum Direction {
        INBOUND,
        OUTBOUND
    }

    public InterceptorChain(ChannelHandlerContext ctx, List<Interceptor> interceptors, ResourceRequest request) {
        this.ctx = ctx;
        this.interceptors = new ArrayList(interceptors);
        this.request = request;
        this.direction = Direction.INBOUND;
    }

    public InterceptorChain(ChannelHandlerContext ctx, List<Interceptor> interceptors, ResourceResponse response) {
        this.ctx = ctx;
        this.interceptors = new ArrayList(interceptors);
        this.response = response;
        this.direction = Direction.OUTBOUND;
    }

    public ResourceRequest request() {
        if (this.request != null) {
            return this.request;
        }
        return this.response.inReplyTo();
    }

    public ResourceResponse response() {
        return this.response;
    }

    public void fireInbound() {
        this.direction = Direction.INBOUND;
        this.current = 0;
        fireCurrentInbound();
    }

    public void fireOutbound() {
        this.direction = Direction.OUTBOUND;
        this.current = this.interceptors.size() - 1;
        fireCurrentOutbound();
    }

    private void fireCurrentInbound() {
        if (this.current > (this.interceptors.size() - 1)) {
            this.ctx.fireChannelRead(this.request);
            return;
        }

        InboundInterceptorContext context = new InboundInterceptorContextImpl(this);
        Interceptor interceptor = this.interceptors.get(this.current);
        try {
            interceptor.onInbound(context);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void fireCurrentOutbound() {
        if (this.current < 0) {
            this.ctx.writeAndFlush(this.response);
            return;
        }

        OutboundInterceptorContext context = new OutboundInterceptorContextImpl(this);
        Interceptor interceptor = this.interceptors.get(this.current);
        try {
            interceptor.onOutbound(context);
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

    public void forward(ResourceRequest request) {
        this.request = request;
        forward();
    }

    public void forward(ResourceResponse response) {
        this.response = response;
        forward();
    }

    public void replyWith(ResourceResponse response) {
        this.direction = Direction.OUTBOUND;
        this.response = response;
        forward();
    }

    private ResourceRequest request;
    private ResourceResponse response;

    private final ChannelHandlerContext ctx;
    private final ArrayList<Interceptor> interceptors;
    private int current = 0;
    private Direction direction;


}
