package org.projectodd.restafari.container.protocols.http;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import org.projectodd.restafari.container.SimpleObjectResource;
import org.projectodd.restafari.container.responses.ErrorResponse;
import org.projectodd.restafari.spi.Resource;

import java.util.List;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 */
public class HttpErrorResponseEncoder extends MessageToMessageEncoder<ErrorResponse> {

    //TODO: Support content, i.e. error messages back to client
    @Override
    protected void encode(ChannelHandlerContext ctx, ErrorResponse error, List<Object> out) throws Exception {
        int statusCode = 0;
        String message = null;

        switch (error.responseType()) {
            case INTERNAL_ERROR:
                statusCode = HttpResponseStatus.INTERNAL_SERVER_ERROR.code();
                break;
            case NOT_FOUND:
                statusCode= HttpResponseStatus.NOT_FOUND.code();
                break;
            case NOT_ALLOWED:
                statusCode= HttpResponseStatus.METHOD_NOT_ALLOWED.code();
                break;
            case INVALID_MIME_TYPE:
                statusCode= HttpResponseStatus.NOT_ACCEPTABLE.code();
                break;
        }

        if (error.message() != null ) {
            message = error.message();
        } else {
            message = HttpResponseStatus.valueOf( statusCode ).reasonPhrase();
        }

        HttpResponseStatus responseStatus = new HttpResponseStatus( statusCode, message);
        DefaultHttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_1, responseStatus);


        // Add headers that might pertain to the corresponding http error
        /*
        for (String headerName : error.headers().keySet()) {
            response.headers().add(headerName, error.headers().get(headerName));
        }
        */

        response.headers().add(HttpHeaders.Names.CONTENT_LENGTH, 0);
        out.add(response);
    }
}
