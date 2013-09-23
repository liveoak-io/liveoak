package org.projectodd.restafari.container.responses;

import org.projectodd.restafari.spi.Resource;

public class ResourceCreatedResponse extends BaseResourceResponse {
    
    public ResourceCreatedResponse(String mimeType, Resource resource) {
        super( mimeType, resource );
    }

}
