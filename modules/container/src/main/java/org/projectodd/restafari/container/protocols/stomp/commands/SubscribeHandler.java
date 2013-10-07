package org.projectodd.restafari.container.protocols.stomp.commands;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import org.projectodd.restafari.container.ResourcePath;
import org.projectodd.restafari.container.protocols.stomp.StompFrame;
import org.projectodd.restafari.container.subscriptions.SubscriptionManager;

/**
 * @author Bob McWhirter
 */
public class SubscribeHandler extends ChannelDuplexHandler {

    public SubscribeHandler(SubscriptionManager subscriptionManager) {
        this.subscriptionManager = subscriptionManager;

    }
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if ( msg instanceof StompFrame && ((StompFrame) msg).getCommand() == StompFrame.Command.SUBSCRIBE ) {
            String subscriptionId = ((StompFrame)msg).getHeader( StompFrame.Header.ID );
            Attribute<String> connectionId = ctx.attr( ConnectHandler.CONNECTION_ID );
            String fullyQualifiedSubscriptionId = connectionId.get() + "/" + subscriptionId;
            ResourcePath path = new ResourcePath( ((StompFrame) msg).getHeader( StompFrame.Header.DESTINATION ) );
            if ( path.isCollectionPath() ) {
                //this.subscriptionManager.createSubscription( fullyQualifiedSubscriptionId, path.getType(), path.getCollectionName() );
            } else if ( path.isResourcePath() ) {
                //this.subscriptionManager.createSubscription( fullyQualifiedSubscriptionId, path.getType(), path.getCollectionName(), path.getResourceId() );
            }

        }
    }

    private SubscriptionManager subscriptionManager;
}
