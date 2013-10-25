package org.projectodd.restafari.container.protocols.http;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import io.netty.handler.codec.http.*;
import org.projectodd.restafari.container.ResourceErrorResponse;
import org.projectodd.restafari.container.ResourceResponse;
import org.projectodd.restafari.container.codec.EncodingResult;
import org.projectodd.restafari.container.codec.IncompatibleMediaTypeException;
import org.projectodd.restafari.container.codec.MediaTypeMatcher;
import org.projectodd.restafari.container.codec.ResourceCodecManager;
import org.projectodd.restafari.spi.MediaType;
import org.projectodd.restafari.spi.resource.Resource;

import java.util.List;

/**
 * @author Bob McWhirter
 */
public class HttpResourceResponseEncoder extends MessageToMessageEncoder<ResourceResponse> {

    public HttpResourceResponseEncoder(ResourceCodecManager codecManager) {
        this.codecManager = codecManager;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, ResourceResponse msg, List<Object> out) throws Exception {
        int responseStatusCode = 0;
        String responseMessage = null;

        System.err.println( "respond with: "+ msg);

        boolean shouldEncodeState = false;
        switch (msg.responseType()) {
            case CREATED:
                responseStatusCode = HttpResponseStatus.CREATED.code();
                responseMessage = HttpResponseStatus.CREATED.reasonPhrase();
                shouldEncodeState = true;
                break;
            case READ:
                responseStatusCode = HttpResponseStatus.OK.code();
                responseMessage = HttpResponseStatus.OK.reasonPhrase();
                shouldEncodeState = true;
                break;
            case UPDATED:
                responseStatusCode = HttpResponseStatus.OK.code();
                responseMessage = HttpResponseStatus.OK.reasonPhrase();
                shouldEncodeState = true;
                break;
            case DELETED:
                responseStatusCode = HttpResponseStatus.OK.code();
                responseMessage = HttpResponseStatus.OK.reasonPhrase();
                shouldEncodeState = true;
                break;
            case ERROR:
                if (msg instanceof ResourceErrorResponse) {
                    switch (((ResourceErrorResponse) msg).errorType()) {
                        case NOT_AUTHORIZED:
                            responseStatusCode = HttpResponseStatus.FORBIDDEN.code();
                            responseMessage = HttpResponseStatus.FORBIDDEN.reasonPhrase();
                            break;
                        case NOT_ACCEPTABLE:
                            responseStatusCode = HttpResponseStatus.NOT_ACCEPTABLE.code();
                            responseMessage = HttpResponseStatus.NOT_ACCEPTABLE.reasonPhrase();
                            break;
                        case NO_SUCH_RESOURCE:
                            responseStatusCode = HttpResponseStatus.NOT_FOUND.code();
                            responseMessage = HttpResponseStatus.NOT_FOUND.reasonPhrase();
                            break;
                        case CREATE_NOT_SUPPORTED:
                            responseStatusCode = HttpResponseStatus.METHOD_NOT_ALLOWED.code();
                            responseMessage = "Create not supported";
                            break;
                        case READ_NOT_SUPPORTED:
                            responseStatusCode = HttpResponseStatus.METHOD_NOT_ALLOWED.code();
                            responseMessage = "Read not supported";
                            break;
                        case UPDATE_NOT_SUPPORTED:
                            responseStatusCode = HttpResponseStatus.METHOD_NOT_ALLOWED.code();
                            responseMessage = "Update not supported";
                            break;
                        case DELETE_NOT_SUPPORTED:
                            responseStatusCode = HttpResponseStatus.METHOD_NOT_ALLOWED.code();
                            responseMessage = "Delete not supported";
                            break;
                    }
                }
                break;
        }

        DefaultFullHttpResponse response = null;
        HttpResponseStatus responseStatus = null;

        EncodingResult encodingResult = null;
        if (shouldEncodeState) {
            System.err.println( "encoding state" );
            MediaTypeMatcher matcher = msg.inReplyTo().mediaTypeMatcher();
            try {
                encodingResult = encodeState(matcher, msg.resource());
            } catch (IncompatibleMediaTypeException e) {
                e.printStackTrace();
                System.err.println("reformulating error response");
                responseStatus = new HttpResponseStatus(HttpResponseStatus.NOT_ACCEPTABLE.code(), e.getMessage() );
                System.err.println( "new response status: " + responseStatus );
                response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, responseStatus );
                System.err.println( "new response " + response );
                response.headers().add(HttpHeaders.Names.CONTENT_LENGTH, 0);
                System.err.println("adding response: " + response);
                out.add( response );
                return;
            }
        }

        responseStatus = new HttpResponseStatus(responseStatusCode, responseMessage);

        if (encodingResult != null) {
            ByteBuf content = encodingResult.encoded();
            response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, responseStatus, content);
            response.headers().add(HttpHeaders.Names.CONTENT_LENGTH, content.readableBytes());
            response.headers().add(HttpHeaders.Names.LOCATION, msg.resource().uri().toString());
            response.headers().add(HttpHeaders.Names.CONTENT_TYPE, encodingResult.mediaType() );
        } else {
            response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, responseStatus);
            response.headers().add(HttpHeaders.Names.CONTENT_LENGTH, 0);
        }

        out.add(response);
    }

    protected EncodingResult encodeState(MediaTypeMatcher mediaTypeMatcher, Resource resource) throws Exception {
        return this.codecManager.encode(mediaTypeMatcher, resource);
    }

    private ResourceCodecManager codecManager;
}
