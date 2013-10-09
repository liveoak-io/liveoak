package org.projectodd.restafari.stomp.client.protocol;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import org.projectodd.restafari.stomp.common.AbstractControlFrameHandler;
import org.projectodd.restafari.stomp.Headers;
import org.projectodd.restafari.stomp.Stomp;
import org.projectodd.restafari.stomp.common.StompControlFrame;
import org.projectodd.restafari.stomp.client.ClientContext;

import java.net.SocketAddress;
import java.util.concurrent.CountDownLatch;

/**
 * @author Bob McWhirter
 */
public class ConnectionNegotiatingHandler extends AbstractControlFrameHandler {

    public ConnectionNegotiatingHandler(ClientContext clientContext) {
        super( Stomp.Command.CONNECTED );
        this.clientContext = clientContext;
    }

    @Override
    public void connect(ChannelHandlerContext ctx, SocketAddress remoteAddress, SocketAddress localAddress, ChannelPromise future) throws Exception {
        this.clientContext.setConnectionState( ClientContext.State.CONNECTING );
        super.connect(ctx, remoteAddress, localAddress, future);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.err.println( "client channel active, send CONNECT" );
        StompControlFrame connectFrame = new StompControlFrame(Stomp.Command.CONNECT );
        connectFrame.setHeader( Headers.HOST, this.clientContext.getHost() );
        connectFrame.setHeader(Headers.ACCEPT_VERSION, Stomp.Version.supportedVersions());
        ctx.write(connectFrame);
        ctx.flush();
    }

    @Override
    protected void handleControlFrame(ChannelHandlerContext ctx, StompControlFrame frame) throws Exception {
        System.err.println( "found CONNECTED" );
        String version = frame.getHeader( Headers.VERSION );
        if (version != null) {
            this.clientContext.setVersion(Stomp.Version.forVersionString(version));
        }

        ctx.pipeline().replace(this, "stomp-disconnection-negotiator", new DisconnectionNegotiatingHandler(this.clientContext));
        this.clientContext.setConnectionState(ClientContext.State.CONNECTED);
        ctx.fireChannelActive();
    }

    private ClientContext clientContext;

}
