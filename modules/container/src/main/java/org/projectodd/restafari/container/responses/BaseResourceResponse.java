package org.projectodd.restafari.container.responses;

import org.projectodd.restafari.spi.Resource;

public class BaseResourceResponse extends BaseResponse {
    
    public BaseResourceResponse(String mimeType, Resource resource) {
        super( mimeType );
        this.resource = resource;
    }
    
    public Resource getResource() {
        return this.resource;
    }

    private Resource resource;

}
