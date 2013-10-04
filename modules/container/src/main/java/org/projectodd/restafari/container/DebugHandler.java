package org.projectodd.restafari.container;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;

/**
 * @author Bob McWhirter
 */
public class DebugHandler extends ChannelDuplexHandler {

    public DebugHandler(String name) {
        this.name = name;

    }
    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        System.err.println( "DEBUG: " + this.name + " write: " + msg );
        super.write(ctx, msg, promise);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        System.err.println( "DEBUG: " + this.name + " read: " + msg );
        super.channelRead(ctx, msg);
    }

    private String name;

}
