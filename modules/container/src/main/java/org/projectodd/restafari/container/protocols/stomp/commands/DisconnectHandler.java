package org.projectodd.restafari.container.protocols.stomp.commands;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.AttributeKey;
import org.projectodd.restafari.container.protocols.stomp.StompControlFrame;
import org.projectodd.restafari.container.protocols.stomp.StompFrame;

import java.util.UUID;

/**
 * @author Bob McWhirter
 */
public class DisconnectHandler extends AbstractControlFrameHandler {

    public DisconnectHandler() {
        super( StompFrame.Command.DISCONNECT );
    }

    @Override
    protected void handleControlFrame(ChannelHandlerContext ctx, StompControlFrame frame) throws Exception {
    }
}
