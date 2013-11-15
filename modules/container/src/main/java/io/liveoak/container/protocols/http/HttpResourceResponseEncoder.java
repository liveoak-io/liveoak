/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.container.protocols.http;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import io.netty.handler.codec.http.*;
import io.liveoak.container.ResourceErrorResponse;
import io.liveoak.container.ResourceResponse;
import io.liveoak.container.codec.EncodingResult;
import io.liveoak.container.codec.IncompatibleMediaTypeException;
import io.liveoak.container.codec.MediaTypeMatcher;
import io.liveoak.container.codec.ResourceCodecManager;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.resource.async.Resource;

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
                        case RESOURCE_ALREADY_EXISTS:
                            responseStatusCode = HttpResponseStatus.NOT_ACCEPTABLE.code();
                            responseMessage = HttpResponseStatus.NOT_ACCEPTABLE.reasonPhrase();
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
                        case INTERNAL_ERROR:
                            responseStatusCode = HttpResponseStatus.INTERNAL_SERVER_ERROR.code();
                            responseMessage = HttpResponseStatus.INTERNAL_SERVER_ERROR.reasonPhrase();
                            break;
                    }
                }
                break;
        }

        DefaultFullHttpResponse response = null;
        HttpResponseStatus responseStatus = null;

        EncodingResult encodingResult = null;
        if (shouldEncodeState) {
            MediaTypeMatcher matcher = msg.inReplyTo().mediaTypeMatcher();
            try {
                encodingResult = encodeState(msg.inReplyTo().requestContext(), matcher, msg.resource());
            } catch (IncompatibleMediaTypeException e) {
                e.printStackTrace();
                responseStatus = new HttpResponseStatus(HttpResponseStatus.NOT_ACCEPTABLE.code(), e.getMessage() );
                response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, responseStatus );
                response.headers().add(HttpHeaders.Names.CONTENT_LENGTH, 0);
                out.add( response );
                return;
            } catch (Throwable e) {
                e.printStackTrace();
                responseStatus = new HttpResponseStatus(HttpResponseStatus.INTERNAL_SERVER_ERROR.code(), HttpResponseStatus.INTERNAL_SERVER_ERROR.reasonPhrase() );
                response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, responseStatus );
                response.headers().add(HttpHeaders.Names.CONTENT_LENGTH, 0);
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

    protected EncodingResult encodeState(RequestContext ctx, MediaTypeMatcher mediaTypeMatcher, Resource resource) throws Exception {
        return this.codecManager.encode(ctx, mediaTypeMatcher, resource);
    }

    private ResourceCodecManager codecManager;
}
