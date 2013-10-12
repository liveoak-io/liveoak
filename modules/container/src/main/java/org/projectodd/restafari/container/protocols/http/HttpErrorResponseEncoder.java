package org.projectodd.restafari.container.protocols.http;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import org.projectodd.restafari.container.responses.HttpErrorResponse;

import java.util.List;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 */
public class HttpErrorResponseEncoder extends MessageToMessageEncoder<HttpErrorResponse> {

    //TODO: Support content, i.e. error messages back to client
    @Override
    protected void encode(ChannelHandlerContext ctx, HttpErrorResponse error, List<Object> out) throws Exception {
        HttpResponseStatus responseStatus;
        if (error.getMessage() == null) {
            responseStatus = HttpResponseStatus.valueOf(error.getCode());
        } else {
            responseStatus = new HttpResponseStatus(error.getCode(), error.getMessage());
        }

        // Add headers that might pertain to the corresponding http error
        DefaultHttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_1, responseStatus);
        for (String headerName : error.headers().keySet()) {
            response.headers().add(headerName, error.headers().get(headerName));
        }

        response.headers().add(HttpHeaders.Names.CONTENT_LENGTH, 0);
        out.add(response);
    }
}
