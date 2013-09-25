package org.projectodd.restafari.container.requests;

import org.projectodd.restafari.container.Container;

public abstract class BaseRequest {
    
    public BaseRequest(String type) {
        this.type = type;
    }
    
    public String getType() {
        return this.type;
    }
    
    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }
    
    public String getMimeType() {
        return this.mimeType;
    }
    
    private String mimeType = "application/json";
    private String type;


}
