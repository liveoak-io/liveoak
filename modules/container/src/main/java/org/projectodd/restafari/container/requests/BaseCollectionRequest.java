package org.projectodd.restafari.container.requests;

import org.projectodd.restafari.container.Container;

public class BaseCollectionRequest extends BaseRequest {

    public BaseCollectionRequest(String type, String collectionName) {
        super(type);
        this.collectionName = collectionName;
    }
    
    public String getCollectionName() {
        return this.collectionName;
    }
    
    private String collectionName;

}
