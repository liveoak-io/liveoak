package org.projectodd.restafari.container.http;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import org.projectodd.restafari.container.codec.ResourceCodecManager;
import org.projectodd.restafari.container.responses.ErrorResponse;

import java.util.List;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 */
public class HttpErrorResponseEncoder extends MessageToMessageEncoder<ErrorResponse> {

    private final ResourceCodecManager codecManager;

    public HttpErrorResponseEncoder(ResourceCodecManager codecManager) {
        this.codecManager = codecManager;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, ErrorResponse error, List<Object> out) throws Exception {
        DefaultHttpResponse response = new DefaultHttpResponse(
            HttpVersion.HTTP_1_1,
            new HttpResponseStatus(HttpResponseStatus.INTERNAL_SERVER_ERROR.code(),
                "Internal Error: " + error.getMessage()));

        response.headers().add(HttpHeaders.Names.CONTENT_LENGTH, 0);
        out.add(response);
    }
}
