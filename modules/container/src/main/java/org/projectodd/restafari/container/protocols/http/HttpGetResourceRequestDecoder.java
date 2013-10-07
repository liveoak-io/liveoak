package org.projectodd.restafari.container.protocols.http;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.util.ReferenceCountUtil;

import java.util.List;

import org.projectodd.restafari.container.Container;
import org.projectodd.restafari.container.ResourcePath;
import org.projectodd.restafari.container.requests.GetResourceRequest;

public class HttpGetResourceRequestDecoder extends MessageToMessageDecoder<HttpRequest> {

    public HttpGetResourceRequestDecoder(Container container) {
        this.container = container;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, HttpRequest msg, List<Object> out) throws Exception {
        System.err.println( "decode: " + msg.getUri() );
        ResourcePath path = new ResourcePath(msg.getUri());
        if (path.isResourcePath() && msg.getMethod() == HttpMethod.GET ) {
            System.err.println( "resource path" );
            GetResourceRequest request = new GetResourceRequest( path.getType(), path.getCollectionName(), path.getResourceId() );
            out.add( request );
        } else {
            System.err.println( "not a resource path" );
            ReferenceCountUtil.retain(msg);
            out.add( msg );
        }
    }

    private Container container;

}
