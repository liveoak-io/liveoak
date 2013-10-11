package org.projectodd.restafari.stomp.server.protocol;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.projectodd.restafari.stomp.StompMessage;
import org.projectodd.restafari.stomp.server.StompServerContext;
import org.projectodd.restafari.stomp.server.StompConnection;

/**
 * @author Bob McWhirter
 */
public class SendHandler extends SimpleChannelInboundHandler<StompMessage> {


    public SendHandler(StompServerContext serverContext) {
        this.serverContext = serverContext;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, StompMessage msg) throws Exception {
        StompMessage stompMessage = (StompMessage) msg;
        StompConnection connection = ctx.channel().attr(ConnectHandler.CONNECTION).get();
        this.serverContext.handleSend(connection, stompMessage);

        // end of the line, do NOT retain or send upstream.
    }

    private StompServerContext serverContext;

}
