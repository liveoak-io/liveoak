package io.liveoak.container.interceptor;

import io.liveoak.container.protocols.RequestCompleteEvent;
import io.liveoak.spi.ResourceRequest;
import io.liveoak.spi.ResourceResponse;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;

/**
 * @author Bob McWhirter
 */
public class InterceptorHandler extends ChannelDuplexHandler {

    public InterceptorHandler(String chainName, InterceptorManagerImpl manager) {
        this.chainName = chainName;
        this.manager = manager;
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        if (msg instanceof ResourceResponse) {
            this.manager.fireOutbound(chainName, ctx, (ResourceResponse) msg);
        } else {
            super.write(ctx, msg, promise);
        }
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof ResourceRequest) {
            this.manager.fireInbound(chainName, ctx, (ResourceRequest) msg);
        } else {
            super.channelRead(ctx, msg);
        }
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if ( evt instanceof RequestCompleteEvent) {
            this.manager.fireComplete(chainName, ((RequestCompleteEvent) evt).requestId());
        }
        super.userEventTriggered(ctx, evt);
    }

    private final InterceptorManagerImpl manager;
    private final String chainName;

}
