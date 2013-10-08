package org.projectodd.restafari.stomp.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.projectodd.restafari.stomp.*;

/**
 * @author Bob McWhirter
 */
public class StompClient {


    public StompClient() {

    }

    public void connect(String host, int port) throws InterruptedException {

        Bootstrap bootstrap = new Bootstrap();
        bootstrap.channel( NioSocketChannel.class );
        bootstrap.handler(new ChannelInitializer<NioSocketChannel>() {
            @Override
            protected void initChannel(NioSocketChannel ch) throws Exception {
            }
        });

        ChannelFuture future = bootstrap.connect(host, port);
        this.channel = future.sync().channel();
    }

    public void send(StompMessage message) {
        this.channel.write( message );
        this.channel.flush();
    }

    public void subscribe(String destination) {
        subscribe( destination, new HeadersImpl() );
    }

    public void subscribe(String destination, Headers headers) {
        StompControlFrame frame = new StompControlFrame(StompFrame.Command.SUBSCRIBE);
        frame.getHeaders().putAll( headers );
        frame.setHeader( StompFrame.Header.DESTINATION, destination );
        this.channel.write( frame );
        this.channel.flush();
    }

    private Channel channel;
}
