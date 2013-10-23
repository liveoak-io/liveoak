package org.projectodd.restafari.container.protocols.http;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.QueryStringDecoder;
import org.projectodd.restafari.container.ResourceParams;
import org.projectodd.restafari.container.ResourcePath;
import org.projectodd.restafari.container.ResourceRequest;
import org.projectodd.restafari.container.codec.ResourceCodecManager;
import org.projectodd.restafari.spi.Pagination;
import org.projectodd.restafari.spi.state.ResourceState;

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

        String authToken = getAuthorizationToken(msg);

        QueryStringDecoder decoder = new QueryStringDecoder(msg.getUri());
        ResourceParams params = ResourceParams.instance(decoder.parameters());

        if (msg.getMethod().equals(HttpMethod.POST)) {
            out.add(new ResourceRequest(ResourceRequest.RequestType.CREATE, new ResourcePath(decoder.path()), params, mimeType, authToken, decodeState(mimeType, msg.content())));
        } else if (msg.getMethod().equals(HttpMethod.GET)) {
            out.add(new ResourceRequest(ResourceRequest.RequestType.READ, new ResourcePath(decoder.path()), params, mimeType, authToken, decodePagination(params)));
        } else if (msg.getMethod().equals(HttpMethod.PUT)) {
            out.add(new ResourceRequest(ResourceRequest.RequestType.UPDATE, new ResourcePath(decoder.path()), params, mimeType, authToken, decodeState(mimeType, msg.content())));
        } else if (msg.getMethod().equals(HttpMethod.DELETE)) {
            out.add(new ResourceRequest(ResourceRequest.RequestType.DELETE, new ResourcePath(decoder.path()), params, mimeType, authToken ));
        }
    }

    protected ResourceState decodeState(String mimeType, ByteBuf content) throws Exception {
        return codecManager.decode( mimeType, content );
    }

    protected Pagination decodePagination(ResourceParams params) {
        int offset = limit(params.getIntValue("offset", 0), 0, Integer.MAX_VALUE);
        int limit = limit(params.getIntValue("limit", Pagination.DEFAULT_LIMIT), 0, Pagination.MAX_LIMIT);

        return new Pagination() {
            public int getOffset() {
                return offset;
            }
            public int getLimit() {
                return limit;
            }
        };
    }

    protected String getAuthorizationToken(FullHttpRequest req) {
        String[] authorization = req.headers().contains(HttpHeaders.Names.AUTHORIZATION) ? req.headers().get(HttpHeaders.Names.AUTHORIZATION).split(" ") : null;
        if (authorization == null) {
            return null;
        } else if (authorization.length != 2 || !authorization[0].equalsIgnoreCase("Bearer")) {
            System.err.println("Authorization header is invalid or it's of different type than 'Bearer'. Ignoring");
            return null;
        } else {
            return authorization[1];
        }
    }

    private static int limit(int value, int lower, int upper) {
        if (value < lower) {
            return lower;
        } else if (value > upper) {
            return upper;
        }
        return value;
    }

    private ResourceCodecManager codecManager;
}
