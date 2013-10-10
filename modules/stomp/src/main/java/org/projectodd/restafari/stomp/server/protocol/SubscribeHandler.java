package org.projectodd.restafari.stomp.server.protocol;

import io.netty.channel.ChannelHandlerContext;
import org.projectodd.restafari.stomp.Headers;
import org.projectodd.restafari.stomp.Stomp;
import org.projectodd.restafari.stomp.common.AbstractControlFrameHandler;
import org.projectodd.restafari.stomp.common.StompControlFrame;
import org.projectodd.restafari.stomp.server.ServerContext;
import org.projectodd.restafari.stomp.server.StompConnection;
import org.projectodd.restafari.stomp.server.StompServerException;

/**
 * @author Bob McWhirter
 */
public class SubscribeHandler extends AbstractControlFrameHandler {

    public SubscribeHandler(ServerContext serverContext) {
        super(Stomp.Command.SUBSCRIBE);
        this.serverContext = serverContext;
    }

    @Override
    public void handleControlFrame(ChannelHandlerContext ctx, StompControlFrame msg) throws StompServerException {
        String subscriptionId = msg.getHeader(Headers.ID );
        String destination = msg.getHeader( Headers.DESTINATION );
        StompConnection stompConnection = ctx.channel().attr(ConnectHandler.CONNECTION).get();
        this.serverContext.handleSubscribe(stompConnection, destination, subscriptionId, msg.getHeaders());
    }

    private ServerContext serverContext;
}
