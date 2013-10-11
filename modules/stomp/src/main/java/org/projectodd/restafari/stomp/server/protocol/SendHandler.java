package org.projectodd.restafari.stomp.server.protocol;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.ReferenceCountUtil;
import org.projectodd.restafari.stomp.Stomp;
import org.projectodd.restafari.stomp.StompMessage;
import org.projectodd.restafari.stomp.common.AbstractFrameHandler;
import org.projectodd.restafari.stomp.common.DefaultStompMessage;
import org.projectodd.restafari.stomp.common.StompContentFrame;
import org.projectodd.restafari.stomp.common.StompFrame;
import org.projectodd.restafari.stomp.server.ServerContext;
import org.projectodd.restafari.stomp.server.StompConnection;

import java.lang.ref.Reference;

/**
 * @author Bob McWhirter
 */
public class SendHandler extends SimpleChannelInboundHandler<StompMessage> {


    public SendHandler(ServerContext serverContext) {
        this.serverContext = serverContext;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, StompMessage msg) throws Exception {
        StompMessage stompMessage = (StompMessage) msg;
        StompConnection connection = ctx.channel().attr(ConnectHandler.CONNECTION).get();
        this.serverContext.handleSend(connection, stompMessage);

        ReferenceCountUtil.retain( msg );
        ctx.fireChannelRead(msg);
    }

    private ServerContext serverContext;

}
