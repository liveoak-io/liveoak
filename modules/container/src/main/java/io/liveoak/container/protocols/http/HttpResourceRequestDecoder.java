/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.container.protocols.http;

import java.net.URI;
import java.util.List;

import io.liveoak.common.DefaultMediaTypeMatcher;
import io.liveoak.common.DefaultResourceParams;
import io.liveoak.common.DefaultResourceRequest;
import io.liveoak.common.codec.DefaultLazyResourceState;
import io.liveoak.common.codec.ResourceCodecManager;
import io.liveoak.common.codec.UnsupportedMediaTypeException;
import io.liveoak.common.DefaultReturnFields;
import io.liveoak.spi.MediaType;
import io.liveoak.spi.MediaTypeMatcher;
import io.liveoak.spi.Pagination;
import io.liveoak.spi.RequestType;
import io.liveoak.spi.ResourceParams;
import io.liveoak.spi.ResourcePath;
import io.liveoak.spi.ReturnFields;
import io.liveoak.spi.Sorting;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.MessageToMessageDecoder;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.DefaultHttpRequest;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.QueryStringDecoder;

/**
 * @author Bob McWhirter
 */
public class HttpResourceRequestDecoder extends MessageToMessageDecoder<DefaultHttpRequest> {

    public static final String HTTP_REQUEST = "HTTP_REQUEST";

    public HttpResourceRequestDecoder(ResourceCodecManager codecManager) {
        this.codecManager = codecManager;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        if (cause instanceof DecoderException) {
            Throwable rootCause = cause.getCause();
            if (rootCause instanceof UnsupportedMediaTypeException) {
                DefaultFullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.NOT_ACCEPTABLE);
                response.headers().add(HttpHeaders.Names.CONTENT_LENGTH, 0);
                ctx.writeAndFlush(response);
                return;
            }
        }
        super.exceptionCaught(ctx, cause);
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, DefaultHttpRequest msg, List<Object> out) throws Exception {

        URI uri = new URI(msg.getUri());
        String query = uri.getRawQuery();
        if (query == null) {
            query = "?";
        } else {
            query = "?" + query;
        }

        QueryStringDecoder decoder = new QueryStringDecoder(query);

        String path = uri.getPath();

        int lastDotLoc = path.lastIndexOf('.');

        String extension = null;

        if (lastDotLoc > 0) {
            extension = path.substring(lastDotLoc + 1);
        }

        String acceptHeader = msg.headers().get(HttpHeaders.Names.ACCEPT);
        if (acceptHeader == null) {
            acceptHeader = "application/json";
        }
        MediaTypeMatcher mediaTypeMatcher = new DefaultMediaTypeMatcher(acceptHeader, extension);

        ResourceParams params = DefaultResourceParams.instance(decoder.parameters());

        // for cases when content is preset, and bypasses HttpRequestBodyHandler
        ByteBuf content = null;
        if (msg instanceof DefaultFullHttpRequest) {
            content = ((DefaultFullHttpRequest) msg).content().retain();
        }

        if (msg.getMethod().equals(HttpMethod.POST)) {
            String contentTypeHeader = msg.headers().get(HttpHeaders.Names.CONTENT_TYPE);
            MediaType contentType = new MediaType(contentTypeHeader);
            out.add(new DefaultResourceRequest.Builder(RequestType.CREATE, new ResourcePath(path))
                    .resourceParams(params)
                    .mediaTypeMatcher(mediaTypeMatcher)
                    .requestAttribute(HttpHeaders.Names.AUTHORIZATION, msg.headers().get(HttpHeaders.Names.AUTHORIZATION))
                    .requestAttribute(HttpHeaders.Names.CONTENT_TYPE, contentType)
                    .requestAttribute(HTTP_REQUEST, msg)
                    .resourceState(new DefaultLazyResourceState(codecManager, contentType, content))
                    .build());
        } else if (msg.getMethod().equals(HttpMethod.GET)) {
            out.add(new DefaultResourceRequest.Builder(RequestType.READ, new ResourcePath(path))
                    .resourceParams(params)
                    .mediaTypeMatcher(mediaTypeMatcher)
                    .requestAttribute(HttpHeaders.Names.AUTHORIZATION, msg.headers().get(HttpHeaders.Names.AUTHORIZATION))
                    .requestAttribute(HTTP_REQUEST, msg)
                    .pagination(decodePagination(params))
                    .returnFields(decodeReturnFields(params))
                    .sorting(decodeSorting(params))
                    .build());
        } else if (msg.getMethod().equals(HttpMethod.PUT)) {
            String contentTypeHeader = msg.headers().get(HttpHeaders.Names.CONTENT_TYPE);
            MediaType contentType = new MediaType(contentTypeHeader);
            out.add(new DefaultResourceRequest.Builder(RequestType.UPDATE, new ResourcePath(path))
                    .resourceParams(params)
                    .mediaTypeMatcher(mediaTypeMatcher)
                    .requestAttribute(HttpHeaders.Names.AUTHORIZATION, msg.headers().get(HttpHeaders.Names.AUTHORIZATION))
                    .requestAttribute(HttpHeaders.Names.CONTENT_TYPE, contentType)
                    .requestAttribute(HTTP_REQUEST, msg)
                    .resourceState(new DefaultLazyResourceState(codecManager, contentType, content))
                    .build());
        } else if (msg.getMethod().equals(HttpMethod.DELETE)) {
            out.add(new DefaultResourceRequest.Builder(RequestType.DELETE, new ResourcePath(path))
                    .resourceParams(params)
                    .mediaTypeMatcher(mediaTypeMatcher)
                    .requestAttribute(HttpHeaders.Names.AUTHORIZATION, msg.headers().get(HttpHeaders.Names.AUTHORIZATION))
                    .requestAttribute(HTTP_REQUEST, msg)
                    .build());
        }
    }

    private ReturnFields decodeReturnFields(ResourceParams params) {
        String fieldsValue = params.value("fields");
        DefaultReturnFields returnFields = null;
        if (fieldsValue != null && !"".equals(fieldsValue)) {
            returnFields = new DefaultReturnFields(fieldsValue);
        } else {
            returnFields = new DefaultReturnFields("*");
        }

        String expandValue = params.value("expand");

        if (expandValue != null && !"".equals(expandValue)) {
            returnFields = returnFields.withExpand(expandValue);
        }

        return returnFields;
    }

    protected Pagination decodePagination(ResourceParams params) {

        int offset = limit(intValue(params.value("offset"), 0), 0, Integer.MAX_VALUE);
        int limit = limit(intValue(params.value("limit"), Pagination.DEFAULT_LIMIT), 0, Pagination.MAX_LIMIT);

        return new Pagination() {
            public int offset() {
                return offset;
            }

            public int limit() {
                return limit;
            }
        };
    }

    protected Sorting decodeSorting(ResourceParams params) {
        String spec = params.value("sort");
        if (spec != null) {
            return new Sorting(spec);
        }
        return new Sorting();
    }

    private static int limit(int value, int lower, int upper) {
        if (value < lower) {
            return lower;
        } else if (value > upper) {
            return upper;
        }
        return value;
    }

    private int intValue(String value, int def) {
        if (value == null) {
            return def;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return def;
        }
    }


    private ResourceCodecManager codecManager;
}
