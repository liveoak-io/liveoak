package org.projectodd.restafari.container.protocols.http;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import org.projectodd.restafari.container.ResourcePath;
import org.projectodd.restafari.container.ResourceRequest;
import org.projectodd.restafari.container.codec.ResourceCodecManager;
import org.projectodd.restafari.spi.state.ResourceState;

import java.io.IOException;
import java.util.List;

/**
 * @author Bob McWhirter
 */
public class HttpResourceRequestDecoder extends MessageToMessageDecoder<FullHttpRequest> {

    public HttpResourceRequestDecoder(ResourceCodecManager codecManager) {
        this.codecManager = codecManager;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, FullHttpRequest msg, List<Object> out) throws Exception {
        String mimeType = msg.headers().get(HttpHeaders.Names.ACCEPT );
        if ( mimeType == null ) {
            mimeType = "application/json";
        }

        if (msg.getMethod().equals(HttpMethod.POST)) {
            out.add(new ResourceRequest(ResourceRequest.RequestType.CREATE, new ResourcePath(msg.getUri()), mimeType, decodeState(mimeType, msg.content())));
        } else if (msg.getMethod().equals(HttpMethod.GET)) {
            out.add(new ResourceRequest(ResourceRequest.RequestType.READ, new ResourcePath(msg.getUri()), mimeType ) );
        } else if (msg.getMethod().equals(HttpMethod.PUT)) {
            out.add(new ResourceRequest(ResourceRequest.RequestType.UPDATE, new ResourcePath(msg.getUri()), mimeType, decodeState(mimeType, msg.content())));
        } else if (msg.getMethod().equals(HttpMethod.DELETE)) {
            out.add(new ResourceRequest(ResourceRequest.RequestType.DELETE, new ResourcePath(msg.getUri()), mimeType ));
        }
    }

    protected ResourceState decodeState(String mimeType, ByteBuf content) throws Exception {
        return codecManager.decode( mimeType, content );
    }

    private ResourceCodecManager codecManager;
}
