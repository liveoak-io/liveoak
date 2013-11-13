package io.liveoak.stomp.client.protocol;

import io.netty.channel.ChannelHandlerContext;
import io.liveoak.stomp.Headers;
import io.liveoak.stomp.Stomp;
import io.liveoak.stomp.client.StompClient;
import io.liveoak.stomp.common.AbstractControlFrameHandler;
import io.liveoak.stomp.common.StompControlFrame;
import io.liveoak.stomp.server.StompServerException;

import java.util.function.Consumer;

/**
 * @author Bob McWhirter
 */
public class ConnectionNegotiatingHandler extends AbstractControlFrameHandler {

    public ConnectionNegotiatingHandler(StompClientContext clientContext, Consumer<StompClient> callback) {
        super(Stomp.Command.CONNECTED);
        this.clientContext = clientContext;
        this.callback = callback;
    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        this.clientContext.setConnectionState(StompClient.ConnectionState.CONNECTING);
        super.channelRegistered(ctx);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        StompControlFrame connectFrame = new StompControlFrame(Stomp.Command.CONNECT);
        connectFrame.headers().put(Headers.HOST, this.clientContext.getHost());
        connectFrame.headers().put(Headers.ACCEPT_VERSION, Stomp.Version.supportedVersions());
        ctx.writeAndFlush(connectFrame);
        super.channelActive(ctx);
    }

    @Override
    protected void handleControlFrame(ChannelHandlerContext ctx, StompControlFrame frame) throws StompServerException {
        String version = frame.headers().get(Headers.VERSION);
        if (version != null) {
            this.clientContext.setVersion(Stomp.Version.forVersionString(version));
        }

        ctx.pipeline().replace(this, "stomp-disconnection-negotiator", new DisconnectionNegotiatingHandler(this.clientContext));
        this.clientContext.setConnectionState(StompClient.ConnectionState.CONNECTED);
        this.clientContext.setChannel(ctx.channel());
        if (this.callback != null) {
            this.callback.accept(clientContext.getClient());
        }
    }

    private StompClientContext clientContext;
    private Consumer<StompClient> callback;

}
