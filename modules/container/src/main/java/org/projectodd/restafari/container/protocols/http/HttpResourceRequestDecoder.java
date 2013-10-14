package org.projectodd.restafari.container.protocols.http;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import io.netty.handler.codec.http.FullHttpRequest;
import org.projectodd.restafari.container.requests.DefaultHttpResourceRequest;

import java.util.List;

/**
 * //TODO: Can't rely on FullHttpRequest if we are to support http chunks
 *
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 */
public class HttpResourceRequestDecoder extends MessageToMessageDecoder<FullHttpRequest> {

    //TODO: Maybe this does more in the future, but for now it's pretty lame
    @Override
    protected void decode(ChannelHandlerContext ctx, FullHttpRequest msg, List<Object> out) throws Exception {
        out.add(new DefaultHttpResourceRequest(msg));
    }
}
