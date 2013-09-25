package org.projectodd.restafari.container.http;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.util.ReferenceCountUtil;

import java.util.List;

import org.projectodd.restafari.container.Container;
import org.projectodd.restafari.container.ResourcePath;
import org.projectodd.restafari.container.requests.GetCollectionRequest;

public class HttpGetCollectionRequestDecoder extends MessageToMessageDecoder<HttpRequest> {

    public HttpGetCollectionRequestDecoder(Container container) {
        this.container = container;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, HttpRequest msg, List<Object> out) throws Exception {
        ResourcePath path = new ResourcePath(msg.getUri());
        if (path.isCollectionPath() ) {
            GetCollectionRequest request = new GetCollectionRequest(path.getType(), path.getCollectionName() );
            out.add( request );
        } else {
            ReferenceCountUtil.retain(msg);
            out.add( msg );
        }
    }

    private Container container;

}
