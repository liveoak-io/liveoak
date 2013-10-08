package org.projectodd.restafari.container.protocols.stomp;

import io.netty.channel.ChannelHandlerContext;
import org.projectodd.restafari.container.ResourcePath;
import org.projectodd.restafari.stomp.AbstractFrameHandler;
import org.projectodd.restafari.stomp.StompContentFrame;
import org.projectodd.restafari.stomp.StompFrame;
import org.projectodd.restafari.container.subscriptions.SubscriptionManager;

/**
 * @author Bob McWhirter
 */
public class SendHandler extends AbstractFrameHandler {


    public SendHandler(SubscriptionManager subscriptionManager) {
        super( StompFrame.Command.SEND );
        this.subscriptionManager = subscriptionManager;

    }
    @Override
    public void handleFrame(ChannelHandlerContext ctx, StompFrame msg) throws Exception {
        if ( msg instanceof StompContentFrame ) {
            ResourcePath path = new ResourcePath( ((StompFrame) msg).getHeader( StompFrame.Header.DESTINATION ) );
        } else {
            ctx.fireChannelRead(msg);
        }

    }

    private SubscriptionManager subscriptionManager;

}
