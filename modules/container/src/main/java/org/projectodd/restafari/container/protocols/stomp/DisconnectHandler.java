package org.projectodd.restafari.container.protocols.stomp;

import io.netty.channel.ChannelHandlerContext;
import org.projectodd.restafari.stomp.AbstractControlFrameHandler;
import org.projectodd.restafari.stomp.StompControlFrame;
import org.projectodd.restafari.stomp.StompFrame;

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
