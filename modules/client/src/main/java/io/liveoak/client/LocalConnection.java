package io.liveoak.client;

import java.net.SocketAddress;

import io.liveoak.client.protocol.LocalResponseHandler;
import io.liveoak.common.protocol.DebugHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.local.LocalAddress;
import io.netty.channel.local.LocalChannel;
import io.netty.channel.nio.NioEventLoopGroup;

/**
 * @author Bob McWhirter
 */
public class LocalConnection implements Connection {

    public LocalConnection(DefaultClient client) {
        this.client = client;
        this.group = new NioEventLoopGroup();
    }

    @Override
    public void connect(SocketAddress address) throws Exception {
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.channel(LocalChannel.class)
                .group(this.group)
                .handler(createHandler());

        ChannelFuture future = bootstrap.connect(address);
        future.sync();

        this.channel = future.channel();
    }

    @Override
    public void close() {
        this.group.shutdownGracefully();
    }

    @Override
    public void write(ClientRequest request) {
        this.channel.writeAndFlush(request);
    }

    protected ChannelHandler createHandler() {
        return new ChannelInitializer<LocalChannel>() {
            protected void initChannel(LocalChannel ch) throws Exception {
                //ch.pipeline().addLast(new DebugHandler( "local-client-head" ) );
                ch.pipeline().addLast(new LocalResponseHandler());
            }
        };
    }

    private DefaultClient client;
    private EventLoopGroup group;
    private Channel channel;
}
