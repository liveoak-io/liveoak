package io.liveoak.container.server;

import java.net.SocketAddress;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.ServerChannel;
import io.netty.channel.local.LocalAddress;
import io.netty.channel.local.LocalChannel;
import io.netty.channel.local.LocalServerChannel;
import io.netty.channel.nio.NioEventLoopGroup;

/**
 * @author Bob McWhirter
 */
public class LocalServer extends AbstractServer {

    public LocalServer() {
        this.group = new NioEventLoopGroup();
    }

    @Override
    protected EventLoopGroup eventLoopGroup() {
        return this.group;
    }

    @Override
    protected Class<? extends ServerChannel> channelClass() {
        return LocalServerChannel.class;
    }

    @Override
    public SocketAddress localAddress() {
        return new LocalAddress("liveoak");
    }

    @Override
    protected ChannelHandler createChildHandler() {
        return new ChannelInitializer<LocalChannel>() {
            protected void initChannel(LocalChannel ch) throws Exception {
                pipelineConfigurator().setupLocal(ch.pipeline());
            }
        };
    }

    private EventLoopGroup group;
}
