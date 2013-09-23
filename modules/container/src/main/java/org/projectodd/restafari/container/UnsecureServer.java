package org.projectodd.restafari.container;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.net.InetAddress;

public class UnsecureServer extends AbstractServer {

    public UnsecureServer(Container container, InetAddress host, int port, EventLoopGroup group) {
        super(container, host, port, group);
    }

    protected ChannelHandler createChildHandler() {
        
        return new ChannelInitializer<NioSocketChannel>() {
            protected void initChannel(NioSocketChannel ch) throws Exception {
                addHttpCodec( ch.pipeline() );
                addHttpResourceResponseEncoders(ch.pipeline() );
                addHttpContainerHandler( ch.pipeline() );
            }
        };
    }


}
