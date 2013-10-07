package org.projectodd.restafari.container.protocols.stomp.commands;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.AttributeKey;
import org.projectodd.restafari.container.ResourcePath;
import org.projectodd.restafari.container.protocols.stomp.StompFrame;
import org.projectodd.restafari.container.subscriptions.SubscriptionManager;

import java.util.UUID;

/**
 * @author Bob McWhirter
 */
public class ConnectHandler extends ChannelDuplexHandler {

    static final AttributeKey<String> CONNECTION_ID = new AttributeKey<>( "connection_id" );

    public ConnectHandler() {
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if ( msg instanceof StompFrame && ((StompFrame) msg).getCommand() == StompFrame.Command.CONNECT ) {
            String connectionId = UUID.randomUUID().toString();
            ctx.attr( CONNECTION_ID ).set( connectionId );
        }
    }
}
