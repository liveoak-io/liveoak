package org.projectodd.restafari.container.protocols.stomp;

import io.netty.channel.ChannelHandlerContext;
import io.netty.util.Attribute;
import org.projectodd.restafari.container.ResourcePath;
import org.projectodd.restafari.stomp.AbstractControlFrameHandler;
import org.projectodd.restafari.stomp.StompControlFrame;
import org.projectodd.restafari.stomp.StompFrame;
import org.projectodd.restafari.container.subscriptions.SubscriptionManager;

/**
 * @author Bob McWhirter
 */
public class SubscribeHandler extends AbstractControlFrameHandler {

    public SubscribeHandler(SubscriptionManager subscriptionManager) {
        super(StompFrame.Command.SUBSCRIBE);
        this.subscriptionManager = subscriptionManager;
    }

    @Override
    public void handleControlFrame(ChannelHandlerContext ctx, StompControlFrame msg) throws Exception {
        String subscriptionId = ((StompFrame) msg).getHeader(StompFrame.Header.ID);
        Attribute<String> connectionId = ctx.attr(ConnectHandler.CONNECTION_ID);
        String fullyQualifiedSubscriptionId = connectionId.get() + "/" + subscriptionId;
        ResourcePath path = new ResourcePath(((StompFrame) msg).getHeader(StompFrame.Header.DESTINATION));
        if (path.isCollectionPath()) {
            //this.subscriptionManager.createSubscription( fullyQualifiedSubscriptionId, path.getType(), path.getCollectionName() );
        } else if (path.isResourcePath()) {
            //this.subscriptionManager.createSubscription( fullyQualifiedSubscriptionId, path.getType(), path.getCollectionName(), path.getResourceId() );
        }
    }

    private SubscriptionManager subscriptionManager;
}
