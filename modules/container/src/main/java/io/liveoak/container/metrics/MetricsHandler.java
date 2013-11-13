package io.liveoak.container.metrics;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;

/**
 * @author Bob McWhirter
 */
public class MetricsHandler extends ChannelDuplexHandler {

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        super.write(ctx, msg, promise);    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        super.channelRead(ctx, msg);    //To change body of overridden methods use File | Settings | File Templates.
    }
}
