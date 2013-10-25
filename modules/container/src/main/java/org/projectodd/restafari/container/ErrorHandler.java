package org.projectodd.restafari.container;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import org.projectodd.restafari.container.codec.UnsupportedMediaTypeException;
import org.projectodd.restafari.container.responders.CreateResponder;
import org.projectodd.restafari.container.responders.DeleteResponder;
import org.projectodd.restafari.container.responders.ReadResponder;
import org.projectodd.restafari.container.responders.UpdateResponder;

public class ErrorHandler extends ChannelDuplexHandler {

    public ErrorHandler() {
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        DefaultFullHttpResponse response = new DefaultFullHttpResponse( HttpVersion.HTTP_1_1, HttpResponseStatus.INTERNAL_SERVER_ERROR );
        response.headers().add(HttpHeaders.Names.CONTENT_LENGTH, 0 );
    }
}
