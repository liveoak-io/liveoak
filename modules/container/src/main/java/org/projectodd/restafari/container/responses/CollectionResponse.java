package org.projectodd.restafari.container.responses;

import org.projectodd.restafari.container.requests.BaseRequest;
import org.projectodd.restafari.spi.Resource;

import java.util.Collection;

/**
 * @author Bob McWhirter
 */
public class CollectionResponse extends BaseResponse {

    public enum ResponseType {
        READ,
    }
    public CollectionResponse(BaseRequest request, ResponseType responseType, Collection<Resource> resources) {
        super( request );
        this.responseType = responseType;
        this.resources = resources;
    }

    public ResponseType responseType() {
        return this.responseType;
    }

    public Collection<Resource> resources() {
        return this.resources;
    }

    private ResponseType responseType;
    private Collection<Resource> resources;
}
