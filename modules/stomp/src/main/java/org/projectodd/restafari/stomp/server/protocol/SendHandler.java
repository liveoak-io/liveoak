package org.projectodd.restafari.stomp.server.protocol;

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
public class SendHandler extends AbstractFrameHandler {


    public SendHandler(ServerContext serverContext) {
        super( Stomp.Command.SEND );
        this.serverContext = serverContext;

    }
    @Override
    public void handleFrame(ChannelHandlerContext ctx, StompFrame msg) throws Exception {
        if ( msg instanceof StompContentFrame) {
            StompConnection connection = ctx.attr(ConnectHandler.CONNECTION ).get();
            StompMessage message = new DefaultStompMessage( msg.getHeaders(), ((StompContentFrame) msg).getContent() );
            this.serverContext.handleSend(connection, message);
        } else {
            ctx.fireChannelRead(msg);
        }

    }

    private ServerContext serverContext;

}
