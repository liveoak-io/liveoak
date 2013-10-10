package org.projectodd.restafari.stomp.common;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.projectodd.restafari.stomp.Stomp;
import org.projectodd.restafari.stomp.server.StompServerException;

/**
 * @author Bob McWhirter
 */
public abstract class AbstractFrameHandler extends SimpleChannelInboundHandler<StompFrame> {

    public AbstractFrameHandler(Stomp.Command command) {
        this.command = command;
    }

    public void channelRead0(ChannelHandlerContext ctx, StompFrame msg) throws Exception {
        if (this.command != null) {
            if (((StompFrame) msg).getCommand().equals(this.command)) {
                handleFrame(ctx, (StompFrame) msg);
                return;
            }
        } else {
            handleFrame(ctx, (StompFrame) msg);
            return;
        }

        ctx.fireChannelRead( msg );
    }

    protected abstract void handleFrame(ChannelHandlerContext ctx, StompFrame frame) throws StompServerException;

    private Stomp.Command command;
}
