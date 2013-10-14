package org.projectodd.restafari.container.responses;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.projectodd.restafari.container.requests.HttpResourceRequest;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 */
public class DefaultHttpResourceResponse implements HttpResourceResponse {

    private final HttpResourceRequest request;
    private final String contentType;
    private final ByteBuf content;

    public DefaultHttpResourceResponse(HttpResourceRequest request, ByteBuf content, String contentType) {
        this.request = request;
        this.content = (content == null) ? Unpooled.EMPTY_BUFFER : content;
        this.contentType = contentType;
    }

    public String getHttpMethod() {
        return request.getHttpMethod();
    }

    @Override
    public String getContentType() {
        return contentType;
    }

    @Override
    public ByteBuf content() {
        return content;
    }
}
