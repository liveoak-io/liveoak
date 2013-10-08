package org.projectodd.restafari.container.protocols.stomp.commands;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import org.projectodd.restafari.container.protocols.stomp.StompControlFrame;
import org.projectodd.restafari.container.protocols.stomp.StompFrame;

/**
 * @author Bob McWhirter
 */
public abstract class AbstractFrameHandler extends ChannelDuplexHandler {

    public AbstractFrameHandler(StompFrame.Command command) {
        this.command = command;
    }

    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if ( msg instanceof StompFrame && ((StompFrame) msg).getCommand().equals( this.command ) ) {
            handleFrame( ctx, (StompFrame) msg );
        } else {
            ctx.fireChannelRead( msg );
        }
    }

    protected abstract void handleFrame(ChannelHandlerContext ctx, StompFrame frame) throws Exception;

    private StompFrame.Command command;
}
