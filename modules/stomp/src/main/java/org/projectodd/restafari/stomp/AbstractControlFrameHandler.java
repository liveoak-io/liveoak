package org.projectodd.restafari.stomp;

import io.netty.channel.ChannelHandlerContext;

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
