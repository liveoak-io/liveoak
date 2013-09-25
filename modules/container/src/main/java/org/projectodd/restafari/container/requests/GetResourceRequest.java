package org.projectodd.restafari.container.requests;

public class GetResourceRequest extends BaseResourceRequest {

    public GetResourceRequest(String type, String collectionName, String resourceId) {
        super(type, collectionName, resourceId);
    }

}
