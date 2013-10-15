package org.projectodd.restafari.container.requests;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.DefaultByteBufHolder;
import org.projectodd.restafari.container.ResourcePath;
import org.projectodd.restafari.spi.Pagination;

/**
 * @author Bob McWhirter
 */
public class CollectionRequest extends BaseRequest {

    public enum RequestType {
        READ,
    }

    public CollectionRequest(RequestType requestType, ResourcePath resourcePath, String mimeType, Pagination pagination)  {
        super( resourcePath, mimeType );
        this.requestType = requestType;
        this.pagination = pagination;
    }

    public RequestType requestType() {
        return this.requestType;
    }

    public Pagination pagination() {
        return this.pagination;
    }

    private RequestType requestType;
    private Pagination pagination;
}
