package io.liveoak.container.analytics;

import io.liveoak.stomp.StompMessage;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;

/**
 * @author <a href="mailto:marko.strukelj@gmail.com">Marko Strukelj</a>
 */
public class AnalyticsNotificationHandler extends ChannelOutboundHandlerAdapter {

    private final AnalyticsBandwidthHandler analyticsHandler;

    public AnalyticsNotificationHandler(AnalyticsBandwidthHandler analyticsHandler) {
        this.analyticsHandler = analyticsHandler;
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        if (msg instanceof StompMessage) {
            analyticsHandler.notificationInfo((StompMessage) msg);
        }
        super.write(ctx, msg, promise);
    }

}
