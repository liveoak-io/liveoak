package org.projectodd.restafari.container.requests;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufHolder;
import io.netty.buffer.DefaultByteBufHolder;
import org.projectodd.restafari.container.ResourcePath;
import org.projectodd.restafari.spi.Pagination;
import org.projectodd.restafari.spi.Resource;

/**
 * @author Bob McWhirter
 */
public class ResourceRequest extends BaseRequest  {

    public enum RequestType {
        CREATE,
        READ,
        UPDATE,
        DELETE
    }

    public ResourceRequest(RequestType requestType, ResourcePath resourcePath, String mimeType) {
        this( requestType, resourcePath, mimeType, null );
    }

    public ResourceRequest(RequestType requestType, ResourcePath resourcePath, String mimeType, Resource resource) {
        super( resourcePath, mimeType );
        this.requestType = requestType;
        this.resource = resource;
    }

    public RequestType requestType() {
        return this.requestType;
    }

    public Resource resource() {
        return this.resource;
    }

    private RequestType requestType;
    private Resource resource;
}
