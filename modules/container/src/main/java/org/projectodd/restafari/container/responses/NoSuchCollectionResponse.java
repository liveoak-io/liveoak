package org.projectodd.restafari.container.responses;

public class NoSuchCollectionResponse extends BaseResponse {
    
    public NoSuchCollectionResponse(String mimeType, String name) {
        super( mimeType );
        this.name = name;
    }
    
    public String getName() {
        return this.name;
    }

    private String name;

}
