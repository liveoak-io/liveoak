/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.container.protocols.http;

import io.liveoak.common.DefaultResourceResponse;
import io.liveoak.common.DefaultResourceErrorResponse;
import io.liveoak.common.codec.EncodingResult;
import io.liveoak.common.codec.IncompatibleMediaTypeException;
import io.liveoak.common.codec.ResourceCodecManager;
import io.liveoak.container.protocols.RequestCompleteEvent;
import io.liveoak.spi.MediaTypeMatcher;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.ResourceErrorResponse;
import io.liveoak.spi.ResourceResponse;
import io.liveoak.spi.resource.async.BinaryContentSink;
import io.liveoak.spi.resource.async.BinaryResource;
import io.liveoak.spi.resource.async.Resource;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.DefaultHttpContent;
import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.DefaultLastHttpContent;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.LastHttpContent;
import org.jboss.logging.Logger;

import java.util.List;

/**
 * @author Bob McWhirter
 */
public class HttpResourceResponseEncoder extends MessageToMessageEncoder<DefaultResourceResponse> {

    public HttpResourceResponseEncoder(ResourceCodecManager codecManager) {
        this.codecManager = codecManager;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, DefaultResourceResponse msg, List<Object> out) throws Exception {

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
                    switch (((DefaultResourceErrorResponse) msg).errorType()) {
                        case NOT_AUTHORIZED:
                            responseStatusCode = HttpResponseStatus.UNAUTHORIZED.code();
                            responseMessage = HttpResponseStatus.UNAUTHORIZED.reasonPhrase();
                            break;
                        case FORBIDDEN:
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
                            responseMessage = "UpdateStep not supported";
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

                    //TODO: add content values here to return proper error messages to the client
                    // eg unique error id, short error message, link to page with more information, etc...
                    //response.content().writeBytes(...)

                }
                break;
        }

        DefaultHttpResponse response = null;
        HttpResponseStatus responseStatus = null;

        EncodingResult encodingResult = null;
        if (shouldEncodeState) {
            MediaTypeMatcher matcher = msg.inReplyTo().mediaTypeMatcher();
            try {
                encodingResult = encodeState(msg.inReplyTo().requestContext(), matcher, msg);
            } catch (IncompatibleMediaTypeException e) {
                log.error("Incompatible media type", e);
                responseStatus = new HttpResponseStatus(HttpResponseStatus.NOT_ACCEPTABLE.code(), e.getMessage());
                response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, responseStatus);
                response.headers().add(HttpHeaders.Names.CONTENT_LENGTH, 0);
                out.add(response);
                return;
            } catch (Throwable e) {
                log.error("Could not encode HTTP response", e);
                responseStatus = new HttpResponseStatus(HttpResponseStatus.INTERNAL_SERVER_ERROR.code(), HttpResponseStatus.INTERNAL_SERVER_ERROR.reasonPhrase());
                response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, responseStatus);
                response.headers().add(HttpHeaders.Names.CONTENT_LENGTH, 0);
                out.add(response);
                return;
            }
        }

        responseStatus = new HttpResponseStatus(responseStatusCode, responseMessage);

        if (encodingResult != null) {

            if (msg.resource() instanceof BinaryResource) {
                BinaryResource bin = (BinaryResource) msg.resource();
                response = new DefaultHttpResponse(HttpVersion.HTTP_1_1, responseStatus);
                response.headers().add(HttpHeaders.Names.CONTENT_LENGTH, bin.contentLength());
                response.headers().add(HttpHeaders.Names.LOCATION, msg.resource().uri().toString());
                response.headers().add(HttpHeaders.Names.CONTENT_TYPE, bin.mediaType());

                final HttpResponse res = response;
                bin.readContent(msg.inReplyTo().requestContext(), new BinaryContentSink() {
                    {
                        ctx.write(res);
                    }

                    @Override
                    public void close() {
                        ctx.writeAndFlush(new DefaultLastHttpContent(Unpooled.EMPTY_BUFFER));
                        ctx.fireUserEventTriggered( new RequestCompleteEvent( msg.requestId() ) );
                    }

                    @Override
                    public void accept(ByteBuf byteBuf) {
                        ctx.write(new DefaultHttpContent(byteBuf));
                    }
                });
                return;

            } else {
                ByteBuf content = encodingResult.encoded();

                response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, responseStatus, content);
                response.headers().add(HttpHeaders.Names.CONTENT_LENGTH, content.readableBytes());
                response.headers().add(HttpHeaders.Names.LOCATION, msg.resource().uri().toString());
                response.headers().add(HttpHeaders.Names.CONTENT_TYPE, encodingResult.mediaType());
            }
        } else {
            response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, responseStatus);
            response.headers().add(HttpHeaders.Names.CONTENT_LENGTH, 0);
        }

        out.add(response);
        ctx.fireUserEventTriggered( new RequestCompleteEvent( msg.requestId() ) );
    }

    protected EncodingResult encodeState(RequestContext ctx, MediaTypeMatcher mediaTypeMatcher, ResourceResponse response) throws Exception {
        return this.codecManager.encode(ctx, mediaTypeMatcher, response);
    }

    private ResourceCodecManager codecManager;

    private static final Logger log = Logger.getLogger(HttpResourceResponseEncoder.class);
}
