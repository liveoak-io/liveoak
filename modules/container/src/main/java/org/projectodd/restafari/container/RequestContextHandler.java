package org.projectodd.restafari.container;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;

/**
 * @author <a href="mailto:marko.strukelj@gmail.com">Marko Strukelj</a>
 */
public class RequestContextHandler extends ChannelOutboundHandlerAdapter {

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        if ( msg instanceof ResourceResponse) {
            ResourceResponse res = (ResourceResponse) msg;
            DefaultRequestContext requestCtx = new DefaultRequestContext(null,
                    res.inReplyTo().pagination(),
                    res.inReplyTo().returnFields(),
                    res.inReplyTo().params());
            DefaultRequestContext.associate(requestCtx);
        }
        super.write( ctx, msg, promise );
    }
}
