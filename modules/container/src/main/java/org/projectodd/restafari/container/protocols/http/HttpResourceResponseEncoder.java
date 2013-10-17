package org.projectodd.restafari.container.protocols.http;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import io.netty.handler.codec.http.*;
import org.projectodd.restafari.container.ResourceErrorResponse;
import org.projectodd.restafari.container.ResourceResponse;
import org.projectodd.restafari.container.codec.ResourceCodecManager;
import org.projectodd.restafari.spi.*;

import java.io.IOException;
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
        HttpResponseStatus responseStatus = null;
        ByteBuf content = null;
        switch (msg.responseType()) {
            case CREATED:
                responseStatus = HttpResponseStatus.CREATED;
                content = encodeState(msg.mimeType(), msg.resource());
                break;
            case READ:
                responseStatus = HttpResponseStatus.OK;
                content = encodeState(msg.mimeType(), msg.resource());
                break;
            case UPDATED:
                responseStatus = HttpResponseStatus.OK;
                content = encodeState(msg.mimeType(), msg.resource());
                break;
            case DELETED:
                responseStatus = HttpResponseStatus.OK;
                content = encodeState(msg.mimeType(), msg.resource());
                break;
            case ERROR:
                if (msg instanceof ResourceErrorResponse) {
                    switch (((ResourceErrorResponse) msg).errorType()) {
                        case NOT_AUTHORIZED:
                            responseStatus = HttpResponseStatus.FORBIDDEN;
                            break;
                        case NOT_ACCEPTABLE:
                            responseStatus = HttpResponseStatus.NOT_ACCEPTABLE;
                            break;
                        case NO_SUCH_RESOURCE:
                            responseStatus = HttpResponseStatus.NOT_FOUND;
                            break;
                        case CREATE_NOT_SUPPORTED:
                            responseStatus = HttpResponseStatus.METHOD_NOT_ALLOWED;
                            break;
                        case READ_NOT_SUPPORTED:
                            responseStatus = HttpResponseStatus.METHOD_NOT_ALLOWED;
                            break;
                        case UPDATE_NOT_SUPPORTED:
                            responseStatus = HttpResponseStatus.METHOD_NOT_ALLOWED;
                            break;
                        case DELETE_NOT_SUPPORTED:
                            responseStatus = HttpResponseStatus.METHOD_NOT_ALLOWED;
                            break;
                    }
                }
                break;
        }

        DefaultFullHttpResponse response = null;

        if (content != null) {
            response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, responseStatus, content);
            response.headers().add(HttpHeaders.Names.CONTENT_LENGTH, content.readableBytes());
            response.headers().add( HttpHeaders.Names.LOCATION, msg.resource().uri().toString() );
        } else {
            response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, responseStatus);
            response.headers().add(HttpHeaders.Names.CONTENT_LENGTH, 0);
        }


        out.add( response );
    }

    protected ByteBuf encodeState(String mimeType, Resource resource) throws Exception {
        return this.codecManager.encode( mimeType, resource );
    }

    private ResourceCodecManager codecManager;
}
