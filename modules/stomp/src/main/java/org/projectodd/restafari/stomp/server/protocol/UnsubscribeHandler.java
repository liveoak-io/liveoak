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
public class UnsubscribeHandler extends AbstractControlFrameHandler {

    public UnsubscribeHandler(ServerContext serverContext) {
        super(Stomp.Command.UNSUBSCRIBE);
        this.serverContext = serverContext;

    }

    @Override
    public void handleControlFrame(ChannelHandlerContext ctx, StompControlFrame msg) throws StompServerException {
        String subscriptionId = msg.getHeader(Headers.ID );
        StompConnection stompConnection = ctx.channel().attr( ConnectHandler.CONNECTION ).get();
        this.serverContext.handleUnsubscribe(stompConnection, subscriptionId);
    }

    private ServerContext serverContext;
}
