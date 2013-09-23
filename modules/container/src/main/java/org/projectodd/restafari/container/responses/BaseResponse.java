package org.projectodd.restafari.container.responses;

public class BaseResponse {
    
    public BaseResponse(String mimeType) {
        this.mimeType = mimeType;
    }
    
    public String getMimeType() {
        return this.mimeType;
    }

    private String mimeType;

}
