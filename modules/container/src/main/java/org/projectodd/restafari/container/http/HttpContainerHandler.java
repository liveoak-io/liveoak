package org.projectodd.restafari.container.http;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;

import org.projectodd.restafari.container.Container;
import org.projectodd.restafari.container.ResponderImpl;
import org.projectodd.restafari.container.Route;
import org.projectodd.restafari.container.responses.NoSuchCollectionResponse;

public class HttpContainerHandler extends ChannelDuplexHandler {

    public HttpContainerHandler(Container container) {
        this.container = container;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

        if (msg instanceof HttpRequest) {
            HttpRequest request = (HttpRequest) msg;
            String path = request.getUri();
            String host = request.headers().get(HttpHeaders.Names.HOST);
            int colonLoc = host.indexOf(":");
            if (colonLoc >= 0) {
                host = host.substring(0, colonLoc);
            }

            HttpMethod method = request.getMethod();

            String mimeType = request.headers().get(HttpHeaders.Names.ACCEPT);

            Route route = this.container.findMatchingRoute(path);

            if (route == null) {
                System.err.println("write a nosuch collection");
                ctx.pipeline().write(new NoSuchCollectionResponse(mimeType, path));
                ctx.pipeline().flush();
                return;
            }

            String remainingPath = path.substring(route.getPath().length());
            String resourceId = null;

            if (remainingPath.length() > 1) {
                resourceId = remainingPath.substring(1);
            }

            if (method == HttpMethod.GET) {
                if (resourceId != null) {
                    System.err.println( "resource: " + resourceId );
                    route.getResource(null, resourceId, new ResponderImpl(mimeType, ctx));
                } else {
                    route.getResources(null, new ResponderImpl(mimeType, ctx));
                }
            } else if (method == HttpMethod.DELETE) {
            } else if (method == HttpMethod.POST) {
            } else if (method == HttpMethod.DELETE) {
            }
        }
    }

    private Container container;
}
