package org.projectodd.restafari.stomp.client.protocol;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import org.projectodd.restafari.stomp.client.StompClient;
import org.projectodd.restafari.stomp.common.AbstractControlFrameHandler;
import org.projectodd.restafari.stomp.Headers;
import org.projectodd.restafari.stomp.Stomp;
import org.projectodd.restafari.stomp.common.StompControlFrame;

import java.net.SocketAddress;
import java.util.function.Consumer;

/**
 * @author Bob McWhirter
 */
public class ConnectionNegotiatingHandler extends AbstractControlFrameHandler {

    public ConnectionNegotiatingHandler(ClientContext clientContext, Consumer<StompClient> callback) {
        super(Stomp.Command.CONNECTED);
        this.clientContext = clientContext;
        this.callback = callback;
    }

    @Override
    public void connect(ChannelHandlerContext ctx, SocketAddress remoteAddress, SocketAddress localAddress, ChannelPromise future) throws Exception {
        this.clientContext.setConnectionState(StompClient.ConnectionState.CONNECTING);
        super.connect(ctx, remoteAddress, localAddress, future);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        StompControlFrame connectFrame = new StompControlFrame(Stomp.Command.CONNECT);
        connectFrame.setHeader(Headers.HOST, this.clientContext.getHost());
        connectFrame.setHeader(Headers.ACCEPT_VERSION, Stomp.Version.supportedVersions());
        ctx.writeAndFlush(connectFrame);
    }

    @Override
    protected void handleControlFrame(ChannelHandlerContext ctx, StompControlFrame frame) throws Exception {
        String version = frame.getHeader(Headers.VERSION);
        if (version != null) {
            this.clientContext.setVersion(Stomp.Version.forVersionString(version));
        }

        ctx.pipeline().replace(this, "stomp-disconnection-negotiator", new DisconnectionNegotiatingHandler(this.clientContext));
        this.clientContext.setConnectionState(StompClient.ConnectionState.CONNECTED);
        this.clientContext.setChannel(ctx.channel());
        ctx.fireChannelActive();
        if (this.callback != null) {
            ctx.executor().execute(() -> {
                this.callback.accept(clientContext.getClient());
            });
        }
    }

    private ClientContext clientContext;
    private Consumer<StompClient> callback;

}
