package org.projectodd.restafari.container.responses;

import org.projectodd.restafari.container.requests.BaseRequest;

/**
 * @author Bob McWhirter
 */
public class BaseResponse {

    public BaseResponse(BaseRequest request) {
        this.request = request;
    }

    public String type() {
        return this.request.type();
    }

    public String collectionName() {
        return this.request.collectionName();
    }

    public String resourceId() {
        return this.request.resourceId();
    }

    public String mimeType() {
        return this.request.mimeType();
    }

    private BaseRequest request;
}
