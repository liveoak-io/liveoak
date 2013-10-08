package org.projectodd.restafari.container.protocols.stomp.commands;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.AttributeKey;
import org.projectodd.restafari.container.ResourcePath;
import org.projectodd.restafari.container.protocols.stomp.StompControlFrame;
import org.projectodd.restafari.container.protocols.stomp.StompFrame;
import org.projectodd.restafari.container.subscriptions.SubscriptionManager;

import java.util.UUID;

/**
 * @author Bob McWhirter
 */
public class ConnectHandler extends AbstractControlFrameHandler {

    static final AttributeKey<String> CONNECTION_ID = new AttributeKey<>("connection_id");

    public ConnectHandler() {
        super(StompFrame.Command.CONNECT);
    }

    @Override
    public void handleControlFrame(ChannelHandlerContext ctx, StompControlFrame msg) throws Exception {
        String connectionId = UUID.randomUUID().toString();
        ctx.attr(CONNECTION_ID).set(connectionId);
    }
}
