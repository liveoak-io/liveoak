package org.projectodd.restafari.container.responses;

import org.projectodd.restafari.container.requests.BaseRequest;
import org.projectodd.restafari.container.requests.ResourceRequest;
import org.projectodd.restafari.spi.Resource;

import java.util.Collection;
import java.util.Collections;

/**
 * @author Bob McWhirter
 */
public class ResourceResponse extends BaseResponse {

    public enum ResponseType {
        CREATED,
        READ,
        UPDATED,
        DELETED;
    }

    public ResourceResponse(BaseRequest request, ResponseType responseType, Resource resource) {
        super(request);
        this.responseType = responseType;
        this.resource = resource;
    }

    public ResponseType responseType() {
        return this.responseType;
    }

    public Resource resource() {
        return this.resource;
    }

    private ResponseType responseType;
    private Resource resource;
}
