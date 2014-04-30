package io.liveoak.container.protocols;

import io.netty.channel.*;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.LastHttpContent;

import java.util.Iterator;
import java.util.Map;

/**
 * @author Bob McWhirter
 */
public class ChannelResetterHandler extends ChannelOutboundHandlerAdapter {


    public ChannelResetterHandler(PipelineConfigurator pipelineConfigurator) {
        this.pipelineConfigurator = pipelineConfigurator;
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        if ( msg instanceof FullHttpResponse || msg instanceof LastHttpContent ) {
            ctx.channel().config().setAutoRead(false);
            promise.addListener( (c)->{
                Map<String, ChannelHandler> handlers = ctx.pipeline().toMap();
                for ( ChannelHandler each : handlers.values() ) {
                    ctx.pipeline().remove( each );
                }
                pipelineConfigurator.switchToHttpWebSockets( ctx.pipeline() );
                ctx.channel().config().setAutoRead(true);
            });
        }

        super.write( ctx, msg, promise );
    }

    private final PipelineConfigurator pipelineConfigurator;

}
