package org.projectodd.restafari.stomp.common;

import io.netty.channel.ChannelHandlerContext;
import io.netty.util.ReferenceCountUtil;
import org.projectodd.restafari.stomp.Stomp;
import org.projectodd.restafari.stomp.server.StompServerException;

/**
 * @author Bob McWhirter
 */
public abstract class AbstractControlFrameHandler extends AbstractFrameHandler {

    public AbstractControlFrameHandler(Stomp.Command command) {
        super(command);
    }

    public void handleFrame(ChannelHandlerContext ctx, StompFrame msg) throws StompServerException {
        if (msg instanceof StompControlFrame) {
            handleControlFrame(ctx, (StompControlFrame) msg);
            return;
        }

        ReferenceCountUtil.retain( msg );
        ctx.fireChannelRead( msg );
    }

    protected abstract void handleControlFrame(ChannelHandlerContext ctx, StompControlFrame frame) throws StompServerException;
}
