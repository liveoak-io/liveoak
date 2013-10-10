package org.projectodd.restafari.stomp.server.protocol;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import org.projectodd.restafari.stomp.Stomp;
import org.projectodd.restafari.stomp.StompMessage;
import org.projectodd.restafari.stomp.common.AbstractFrameHandler;
import org.projectodd.restafari.stomp.common.DefaultStompMessage;
import org.projectodd.restafari.stomp.common.StompContentFrame;
import org.projectodd.restafari.stomp.common.StompFrame;
import org.projectodd.restafari.stomp.server.ServerContext;
import org.projectodd.restafari.stomp.server.StompConnection;

/**
 * @author Bob McWhirter
 */
public class SendHandler extends ChannelDuplexHandler {


    public SendHandler(ServerContext serverContext) {
        this.serverContext = serverContext;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof StompMessage) {
            StompMessage stompMessage = (StompMessage) msg;
            StompConnection connection = ctx.attr(ConnectHandler.CONNECTION).get();
            this.serverContext.handleSend(connection, stompMessage);
        } else {
            super.channelRead(ctx, msg);
        }
    }

    private ServerContext serverContext;

}
