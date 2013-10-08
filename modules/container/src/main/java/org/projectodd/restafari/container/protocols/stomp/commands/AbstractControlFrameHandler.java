package org.projectodd.restafari.container.protocols.stomp.commands;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import org.projectodd.restafari.container.protocols.stomp.StompControlFrame;
import org.projectodd.restafari.container.protocols.stomp.StompFrame;

/**
 * @author Bob McWhirter
 */
public abstract class AbstractControlFrameHandler extends AbstractFrameHandler {

    public AbstractControlFrameHandler(StompFrame.Command command) {
        super(command);
    }

    public void handleFrame(ChannelHandlerContext ctx, StompFrame msg) throws Exception {
        if (msg instanceof StompControlFrame) {
            handleControlFrame(ctx, (StompControlFrame) msg);
        } else {
            ctx.fireChannelRead(msg);
        }
    }

    protected abstract void handleControlFrame(ChannelHandlerContext ctx, StompControlFrame frame) throws Exception;
}
