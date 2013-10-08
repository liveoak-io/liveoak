package org.projectodd.restafari.container.protocols.stomp.commands;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import org.projectodd.restafari.container.ResourcePath;
import org.projectodd.restafari.container.protocols.stomp.StompControlFrame;
import org.projectodd.restafari.container.protocols.stomp.StompFrame;
import org.projectodd.restafari.container.subscriptions.SubscriptionManager;

/**
 * @author Bob McWhirter
 */
public class UnsubscribeHandler extends AbstractControlFrameHandler {

    public UnsubscribeHandler(SubscriptionManager subscriptionManager) {
        super(StompFrame.Command.UNSUBSCRIBE);
        this.subscriptionManager = subscriptionManager;

    }

    @Override
    public void handleControlFrame(ChannelHandlerContext ctx, StompControlFrame msg) throws Exception {
        ResourcePath path = new ResourcePath(((StompFrame) msg).getHeader(StompFrame.Header.DESTINATION));
        if (path.isCollectionPath()) {
            //this.subscriptionManager.createSubscription( subId, path.getType(), path.getCollectionName() );
        } else if (path.isResourcePath()) {
            //this.subscriptionManager.createSubscription( subId, path.getType(), path.getCollectionName(), path.getResourceId() );
        }
    }

    private SubscriptionManager subscriptionManager;
}
