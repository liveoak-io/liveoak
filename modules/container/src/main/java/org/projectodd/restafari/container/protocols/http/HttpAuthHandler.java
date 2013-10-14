package org.projectodd.restafari.container.protocols.http;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import org.projectodd.restafari.container.auth.JsonWebToken;

/**
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
public class HttpAuthHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest req) throws Exception {
        // TODO Skip if resource doesn't require auth

        String[] authorization = req.headers().contains(HttpHeaders.Names.AUTHORIZATION) ? req.headers().get(HttpHeaders.Names.AUTHORIZATION).split(" ") : null;

        if (authorization == null || authorization.length != 2 || !authorization[0].equals("bearer")) {
            error(ctx, req, HttpResponseStatus.FORBIDDEN);
            return;
        } else {


            // TODO Verify token
            // RSAProvider.verify(input, realmKey);

            // TODO Check if authorized

            JsonWebToken token = new JsonWebToken(authorization[1]);
            JsonWebToken.Claims claims = token.getClaims();

            String[] resource = req.getUri().split("/");
            if (!claims.getRealmAccess().getRoles().contains(resource[1])) {
                error(ctx, req, HttpResponseStatus.FORBIDDEN);
                return;
            }

            ctx.fireChannelRead(req);
        }
    }

    protected void error(ChannelHandlerContext ctx, Object msg, HttpResponseStatus status) {
        DefaultHttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_1,
                new HttpResponseStatus(HttpResponseStatus.FORBIDDEN.code(), HttpResponseStatus.FORBIDDEN.reasonPhrase()));

        response.headers().add(HttpHeaders.Names.CONTENT_LENGTH, 0);
        ctx.pipeline().write(response);
        ctx.pipeline().flush();
    }
}
