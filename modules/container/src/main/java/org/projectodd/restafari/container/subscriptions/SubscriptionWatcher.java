package org.projectodd.restafari.container.subscriptions;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import io.netty.channel.SimpleChannelInboundHandler;
import org.projectodd.restafari.container.responses.ResourceResponse;

/**
 * @author Bob McWhirter
 */
public class SubscriptionWatcher extends ChannelOutboundHandlerAdapter {

    public SubscriptionWatcher(SubscriptionManager subscriptionManager) {
        this.subscriptionManager = subscriptionManager;
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        if ( msg instanceof ResourceResponse ) {
            ResourceResponse response = (ResourceResponse) msg;

            switch (response.responseType()) {
                case CREATED:
                    this.subscriptionManager.resourceCreated(response.type(), response.collectionName(), response.resource() );
                    break;
                case READ:
                    // no notification
                    break;
                case UPDATED:
                    this.subscriptionManager.resourceUpdated(response.type(), response.collectionName(), response.resource() );
                    break;
                case DELETED:
                    this.subscriptionManager.resourceDeleted(response.type(), response.collectionName(), response.resource() );
                    break;
            }
        }

        super.write( ctx, msg, promise );
    }

    private SubscriptionManager subscriptionManager;

}
