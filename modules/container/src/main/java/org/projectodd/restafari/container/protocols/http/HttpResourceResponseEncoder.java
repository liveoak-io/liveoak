package org.projectodd.restafari.container.protocols.http;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import io.netty.handler.codec.http.*;
import org.projectodd.restafari.container.ResourceErrorResponse;
import org.projectodd.restafari.container.ResourceResponse;
import org.projectodd.restafari.container.codec.ResourceCodecManager;
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
        System.err.println( "respond! " + msg + " // " + msg.inReplyTo().mimeType() );
        ByteBuf content = null;
        int responseStatusCode = 0;
        String responseMessage = null;
        switch (msg.responseType()) {
            case CREATED:
                responseStatusCode = HttpResponseStatus.CREATED.code();
                responseMessage = HttpResponseStatus.CREATED.reasonPhrase();
                content = encodeState(msg.mimeType(), msg.resource());
                break;
            case READ:
                responseStatusCode = HttpResponseStatus.OK.code();
                responseMessage = HttpResponseStatus.OK.reasonPhrase();
                content = encodeState(msg.mimeType(), msg.resource());
                break;
            case UPDATED:
                responseStatusCode = HttpResponseStatus.OK.code();
                responseMessage = HttpResponseStatus.OK.reasonPhrase();
                content = encodeState(msg.mimeType(), msg.resource());
                break;
            case DELETED:
                responseStatusCode = HttpResponseStatus.OK.code();
                responseMessage = HttpResponseStatus.OK.reasonPhrase();
                content = encodeState(msg.mimeType(), msg.resource());
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

        HttpResponseStatus responseStatus = new HttpResponseStatus( responseStatusCode, responseMessage );

        if (content != null) {
            response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, responseStatus, content);
            response.headers().add(HttpHeaders.Names.CONTENT_LENGTH, content.readableBytes());
            response.headers().add( HttpHeaders.Names.LOCATION, msg.resource().uri().toString() );
        } else {
            response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, responseStatus);
            response.headers().add(HttpHeaders.Names.CONTENT_LENGTH, 0);
        }
        response.headers().add( HttpHeaders.Names.CONTENT_TYPE, msg.inReplyTo().mimeType() );



        out.add( response );
    }

    protected ByteBuf encodeState(String mimeType, Resource resource) throws Exception {
        return this.codecManager.encode( mimeType, resource );
    }

    private ResourceCodecManager codecManager;
}
