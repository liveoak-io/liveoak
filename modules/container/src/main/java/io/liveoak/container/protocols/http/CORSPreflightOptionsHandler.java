package io.liveoak.container.protocols.http;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;

/**
 * @author Bob McWhirter
 */
public class CORSPreflightOptionsHandler extends SimpleChannelInboundHandler<HttpRequest> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, HttpRequest msg) throws Exception {
        if (msg.getMethod().equals(HttpMethod.OPTIONS)) {
            DefaultFullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
            response.headers().add("Access-Control-Allow-Headers", msg.headers().getAll("Access-Control-Request-Headers"));
            response.headers().add("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, HEAD");
            response.headers().add("Access-Control-Allow-Origin", msg.headers().get("Origin"));
            response.headers().add("Content-Length", 0);
            ctx.writeAndFlush(response);
            ctx.read();
            return;
        }
        ctx.fireChannelRead(msg);
    }
}
