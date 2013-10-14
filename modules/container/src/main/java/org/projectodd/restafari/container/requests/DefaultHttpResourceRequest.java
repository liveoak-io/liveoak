package org.projectodd.restafari.container.requests;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.DecoderResult;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.QueryStringDecoder;
import org.projectodd.restafari.container.ResourcePath;
import org.projectodd.restafari.spi.Pagination;

import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 */
public class DefaultHttpResourceRequest implements HttpResourceRequest {
    private final FullHttpRequest request;
    private final ResourcePath resourcePath;
    private final Pagination pagination;

    public DefaultHttpResourceRequest(FullHttpRequest request) {
        this.request = request;
        QueryStringDecoder decoder = new QueryStringDecoder(request.getUri());
        this.resourcePath = new ResourcePath(decoder.path());
        int limit = getIntValue("limit", decoder.parameters(), 100);
        int offset = getIntValue("offset", decoder.parameters(), 0);
        this.pagination = new Pagination() {
            @Override
            public int getOffset() {
                return offset;
            }

            @Override
            public int getLimit() {
                return limit;
            }
        };
    }

    @Override
    public ResourcePath getResourcePath() {
        return resourcePath;
    }

    @Override
    public Pagination getPagination() {
        return pagination;
    }

    @Override
    public String getMimeType() {
        String ctHeader = headers().get(HttpHeaders.Names.CONTENT_TYPE);
        if (ctHeader == null || "*/*".equals(ctHeader)) {
            ctHeader = "application/json"; // default to json
        }

        return ctHeader;
    }

    @Override
    public FullHttpRequest copy() {
        return request.copy();
    }

    @Override
    public FullHttpRequest retain(int increment) {
        return request.retain(increment);
    }

    @Override
    public FullHttpRequest retain() {
        return request.retain();
    }

    @Override
    public FullHttpRequest setProtocolVersion(HttpVersion version) {
        return request.setProtocolVersion(version);
    }

    @Override
    public FullHttpRequest setMethod(HttpMethod method) {
        return request.setMethod(method);
    }

    @Override
    public FullHttpRequest setUri(String uri) {
        return request.setUri(uri);
    }

    @Override
    public HttpMethod getMethod() {
        return request.getMethod();
    }

    @Override
    public String getUri() {
        return request.getUri();
    }

    @Override
    public HttpVersion getProtocolVersion() {
        return request.getProtocolVersion();
    }

    @Override
    public HttpHeaders headers() {
        return request.headers();
    }

    @Override
    public DecoderResult getDecoderResult() {
        return request.getDecoderResult();
    }

    @Override
    public void setDecoderResult(DecoderResult result) {
        request.setDecoderResult(result);
    }

    @Override
    public HttpHeaders trailingHeaders() {
        return request.trailingHeaders();
    }

    @Override
    public HttpContent duplicate() {
        return request.duplicate();
    }

    @Override
    public ByteBuf content() {
        return request.content();
    }

    @Override
    public int refCnt() {
        return request.refCnt();
    }

    @Override
    public boolean release() {
        return request.release();
    }

    @Override
    public boolean release(int decrement) {
        return request.release(decrement);
    }

    private static int getIntValue(String key, Map<String, List<String>> parameters, int defaultValue) {
        List<String> values = parameters.get(key);
        if (values != null && values.size() > 0) {
            String value = values.get(0);
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException e) {
                // ignore
            }
        }

        return defaultValue;
    }
}
