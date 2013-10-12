package org.projectodd.restafari.container.protocols.http;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import org.projectodd.restafari.container.responses.HttpResourceResponse;

import java.util.List;

public class HttpResourceResponseEncoder extends MessageToMessageEncoder<HttpResourceResponse> {

    @Override
    protected void encode(ChannelHandlerContext ctx, HttpResourceResponse msg, List<Object> out) throws Exception {
        HttpResponseStatus status = HttpResponseStatus.OK;
        if (HttpMethod.POST.name().equalsIgnoreCase(msg.getHttpMethod())) {
            status = HttpResponseStatus.CREATED;
        }

        DefaultFullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status, msg.content());
        response.headers().add(HttpHeaders.Names.CONTENT_LENGTH, msg.content().readableBytes());
        response.headers().add(HttpHeaders.Names.CONTENT_TYPE, msg.getContentType());
        out.add(response);
    }
}
