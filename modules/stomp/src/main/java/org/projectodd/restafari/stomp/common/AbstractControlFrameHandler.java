package org.projectodd.restafari.stomp.common;

import io.netty.channel.ChannelHandlerContext;
import org.projectodd.restafari.stomp.Stomp;

/**
 * @author Bob McWhirter
 */
public abstract class AbstractControlFrameHandler extends AbstractFrameHandler {

    public AbstractControlFrameHandler(Stomp.Command command) {
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
