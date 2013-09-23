package org.projectodd.restafari.container.responses;

public class NoSuchResourceResponse extends BaseResponse {

    public NoSuchResourceResponse(String mimeType, String id) {
        super( mimeType );
        this.id = id;
    }
    
    public String getId() {
        return this.id;
    }
    
    private String id;

}
