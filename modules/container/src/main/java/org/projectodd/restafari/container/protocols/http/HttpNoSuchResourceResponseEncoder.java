package org.projectodd.restafari.container.protocols.http;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;

import java.util.List;

import org.projectodd.restafari.container.codec.ResourceCodecManager;
import org.projectodd.restafari.container.responses.NoSuchResourceResponse;

public class HttpNoSuchResourceResponseEncoder extends MessageToMessageEncoder<NoSuchResourceResponse> {

    public HttpNoSuchResourceResponseEncoder(ResourceCodecManager codecManager) {
        this.codecManager = codecManager;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, NoSuchResourceResponse msg, List<Object> out) throws Exception {
        DefaultHttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_1, new HttpResponseStatus( HttpResponseStatus.NOT_FOUND.code(), "No such resource: " + msg.getId() ) );
        response.headers().add(HttpHeaders.Names.CONTENT_LENGTH, 0);
        out.add(response);
    }

    private ResourceCodecManager codecManager;

}
