package io.liveoak.container.protocols.http;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.http.HttpResponse;

/**
 * @author Bob McWhirter
 */
public class CORSHandler extends ChannelOutboundHandlerAdapter {
    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        if ( msg instanceof HttpResponse) {
            if ( ! ((HttpResponse) msg).headers().contains( "Access-Control-Allow-Origin" ) ) {
                ((HttpResponse) msg).headers().add( "Access-Control-Allow-Origin", "*" );
            }
        }
        super.write( ctx, msg, promise );
    }
}

