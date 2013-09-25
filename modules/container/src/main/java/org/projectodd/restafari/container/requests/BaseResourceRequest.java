package org.projectodd.restafari.container.requests;

public class BaseResourceRequest extends BaseCollectionRequest {

    public BaseResourceRequest(String type, String collectionName, String resourceId) {
        super(type, collectionName);
        this.resourceId = resourceId;
    }
    
    public String getResourceId() {
        return this.resourceId;
    }

    private String resourceId;

}
